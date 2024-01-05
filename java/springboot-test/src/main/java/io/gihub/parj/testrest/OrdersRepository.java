package io.gihub.parj.testrest;

import org.springframework.data.jpa.repository.JpaRepository;
// Repository interface for Item entity
interface OrdersRepository extends JpaRepository<Orders, Long> {
}
