package org.github.hrautoassignertaskhoursforecast.teamMembers.application.service

import org.github.hrautoassignertaskhoursforecast.teamMembers.application.TeamMemberMapper
import org.github.hrautoassignertaskhoursforecast.teamMembers.application.dto.TeamMemberResponseDTO

import org.github.hrautoassignertaskhoursforecast.teamMembers.infrastructure.jdbc.TeamMemberRepository
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.transaction.annotation.Transactional

@Service
class TeamMemberRankingService(
    private val teamMemberRepository: TeamMemberRepository, // TeamMember 데이터 접근을 위한 Repository
    private val redisTemplate: RedisTemplate<String, Any>,   // Redis 데이터 캐싱을 위한 RedisTemplate
    private val teamMemberMapper: TeamMemberMapper // Mapper를 통한 Entity-DTO 변환 처리
) {

    // Redis Sorted Set 키 값: 주간 및 월간 achievementsScore 순위 데이터를 저장하는 키
    private val MONTHLY_KEY = "team:member:achievements:monthly" // 월간 순위용 Redis 키


    @Transactional(readOnly = true)
    fun getTop3AchievementsMonthly(): List<TeamMemberResponseDTO> {
        return getTopAchievements(MONTHLY_KEY)
    }

    /**
     * Redis에서 상위 팀원 ID를 조회하고 DB에서 해당 팀원 정보를 가져온 후 변환하여 반환한다.
     * @param redisKey Redis Sorted Set의 키 (주간/월간 구분)
     */
    private fun getTopAchievements(redisKey: String): List<TeamMemberResponseDTO> {
        val ops = redisTemplate.opsForZSet() // Redis의 ZSet 연산 객체

        // Redis에서 상위 3명의 팀원 ID를 내림차순으로 조회
        val top3Ids = ops.reverseRange(redisKey, 0, 2) ?: emptySet<Any>()

        // 데이터가 없는 경우 빈 리스트 반환
        if (top3Ids.isEmpty()) {
            return emptyList()
        }

        // 조회된 팀원 ID를 Long으로 변환하고 DB에서 팀원 정보 가져오기
        val members = teamMemberRepository.findAllById(top3Ids.map { it.toString().toLong() })

        // DTO로 변환하여 반환
        return members.map { teamMemberMapper.toResponseDto(it) }
    }


    /**
     * 월간 순위 데이터를 Redis에 업데이트하는 메서드
     * - 스케줄링: 매월 1일 0시(UTC) 실행
     * - Redis의 기존 월간 키 데이터를 삭제한다.
     * - DB에서 모든 팀원 데이터를 가져와 achievementsScore를 기준으로 Redis Sorted Set에 저장한다.
     */
    @Scheduled(cron = "0 0 0 1 * ?", zone = "UTC") // 매월 1일 0시에 실행
    @Transactional
    fun updateMonthlyScoresInRedis() {
        val ops = redisTemplate.opsForZSet() // Redis의 ZSet 연산 객체

        // Redis에서 기존 월간 키 데이터 삭제
        redisTemplate.delete(MONTHLY_KEY)

        // DB에서 모든 팀원 정보를 가져오기
        val members = teamMemberRepository.findAll()

        // 팀원의 achievementsScore를 Redis Sorted Set에 추가
        members.forEach { member ->
            ops.add(MONTHLY_KEY, member.id.toString(), member.achievementsScore.toDouble())
        }
    }

}