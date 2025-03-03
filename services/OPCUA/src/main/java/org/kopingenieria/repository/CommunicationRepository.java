package org.kopingenieria.repository;

import org.kopingenieria.domain.classes.Communication;
import org.kopingenieria.domain.enums.client.network.connection.ProtocolType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import java.time.LocalDateTime;

@Repository
public interface CommunicationRepository extends MongoRepository<Communication, String>, QueryByExampleExecutor<Communication> {

    @Query(value = "{'sessionId': ?0, 'timestamp': {$gte: ?1, $lte: ?2}}",
            sort = "{'timestamp': -1}")
    Flux<Communication> findBySessionIdAndTimeRange(
            String sessionId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Pageable pageable
    );

    @Aggregation(pipeline = {
            "{ $match: { 'clientId': ?0 } }",
            "{ $group: { _id: '$protocolType', count: { $sum: 1 } } }"
    })
    Flux<ProtocolType> getProtocolStatsByClient(String clientId);
}
