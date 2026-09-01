package org.example.matcheat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class RequiresNewTransactionConfig {

	@Bean
	public TransactionTemplate requiresNewTransactionTemplate(PlatformTransactionManager txManager) {
		TransactionTemplate template = new TransactionTemplate(txManager);
		// INSERT 시도를 별도 물리 트랜잭션으로 격리한다.
		// 유니크 제약 위반이 나도 바깥(원래) 트랜잭션은 중단되지 않아야
		// 이어지는 재조회(findByChatRoomId)가 정상 동작한다 — Postgres 전용 이슈.
		template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		return template;
	}
}