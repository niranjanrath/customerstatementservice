package nl.rabobank.customerstatementservice.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerStatementRecordRepository extends JpaRepository<CustomerStatementRecord, Integer> {

}
