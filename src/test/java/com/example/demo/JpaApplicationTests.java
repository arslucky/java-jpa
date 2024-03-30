package com.example.demo;

import com.example.demo.model.Contract;
import com.example.demo.model.Customer;
import com.example.demo.model.Dual;
import com.example.demo.repository.CustomerRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class JpaApplicationTests {

	@Autowired
	private EntityManager entityManager;
	private CriteriaBuilder builder;
	@Autowired
	private CustomerRepository customerRepository;

	@BeforeEach
	void init() {
		builder = entityManager.getCriteriaBuilder();
	}

	@Test
	void contextLoads() {
		System.out.println("**********************************************");
		var res = customerRepository.findAll().stream().collect(Collectors.toList());
		System.out.println(res);
		System.out.println("**********************************************");
	}
	//******************************************************************************//
	@Test
	void criteriaSelect() {
		var query = builder.createQuery(Customer.class);
		var root = query.from(Customer.class);
		query.select(root);
		var res = entityManager.createQuery(query).getResultList();
		log.info("result: {}", res);
		assertNotNull(res);
	}

	@Test
	void jpqlSelect() {
		var query = entityManager.createQuery("from Customer");
		var res = query.getResultList();
		log.info("result: {}", res);
		assertNotNull(res);
	}

	@Test
	void nativeSelect() {
		var query = entityManager.createNativeQuery("select * from customer");
		var res = query.getResultList();
		log.info("result: {}", res);
		assertNotNull(res);
	}
	//******************************************************************************//
	@Test
	void criteriaMax() {
		var cq = builder.createQuery(Long.class);
		var root = cq.from(Contract.class);
		cq.select(builder.max(root.get("id")));
		var res = entityManager.createQuery(cq).getSingleResult();
		log.info("result: {}", res);
		assertNotNull(res);
		assertTrue(res > 1L);
	}

	@Test
	void jpqlMax() {
		var query = entityManager.createQuery("select max(id) from Customer", Long.class);
		var res = query.getSingleResult();
		log.info("result: {}", res);
		assertNotNull(res);
		assertTrue(res > 1L);
	}
	//******************************************************************************//
	@Test
	void criteriaMaxAndGroupBy() {
		var tq = builder.createTupleQuery();
		var root = tq.from(Contract.class);
		tq.multiselect(root.get("customer").get("id").alias("customer_id"), builder.max(root.get("id")).alias("max_contract_id"))
				.groupBy(root.get("customer"))
				.orderBy(builder.asc(root.get("customer")));
		var res = entityManager.createQuery(tq).getResultList();
		res.stream().forEach(r->log.info("customer id: {} with max contract id: {}", r.get("customer_id"), r.get("max_contract_id")));
		assertEquals(res.get(0).get("customer_id"), 1L);
		assertEquals(res.get(0).get("max_contract_id"), 3L);
		assertEquals(res.get(1).get("customer_id"), 2L);
		assertEquals(res.get(1).get("max_contract_id"), 5L);
	}
	//******************************************************************************//
	@Test
	void jpqlJoinMax() {
		var query = entityManager.createQuery(
				"select cust as customer, d.max_contract_id as max_contract_id " +
						"from Customer cust " +
						"join (select c.customer.id as cust_id, max(c.id) as max_contract_id from Contract c group by c.customer.id) d " +
						"where cust.id = d.cust_id", Tuple.class);
		var res = query.getResultList();
		res.stream().forEach(r->log.info("customer: {} with max contract id: {}", r.get("customer"), r.get("max_contract_id")));
		assertEquals(((Customer) res.get(0).get("customer")).getId(), 1L);
		assertEquals(res.get(0).get("max_contract_id"), 3L);
		assertEquals(((Customer) res.get(1).get("customer")).getId(), 2L);
		assertEquals(res.get(1).get("max_contract_id"), 5L);
	}

	@Test
	void hqlJoinMaxByLateral() {
		var query = entityManager.createQuery(
				"select cust as customer, d.max_contract_id as max_contract_id " +
						"from Customer cust " +
						"join lateral (select c.id as max_contract_id from Contract c where c.customer.id = cust.id order by c.id desc limit 1) d " +
						"order by cust.id", Tuple.class);
		var res = query.getResultList();
		res.stream().forEach(r->log.info("customer: {} with max contract id: {}", r.get("customer"), r.get("max_contract_id")));
		assertEquals(((Customer) res.get(0).get("customer")).getId(), 1L);
		assertEquals(res.get(0).get("max_contract_id"), 3L);
		assertEquals(((Customer) res.get(1).get("customer")).getId(), 2L);
		assertEquals(res.get(1).get("max_contract_id"), 5L);
	}
	//******************************************************************************//
	@Test
	void criteriaJoinByWhere() {
		var query = builder.createQuery(Tuple.class);
		var customerRoot = query.from(Customer.class);
		var contractRoot = query.from(Contract.class);

		query.multiselect(customerRoot.alias("customer"), contractRoot.get("id").alias("contract_id"));
		var joinRestriction = builder.equal(customerRoot.get("id"), contractRoot.get("customer"));
		query.where(joinRestriction);
		var res = entityManager.createQuery(query).getResultList();
		res.forEach(r->log.info("customer: {} with contract_id: {}", r.get("customer"), r.get("contract_id")));
		assertFalse(res.isEmpty());
		assertTrue(res.size() > 1);
	}

	@Test
	void jpqlJoinByWhere() {
		var query = entityManager.createQuery(
				"select cust as cust, cont.id as contract_id from Customer cust " +
					"join Contract cont where cust.id = cont.customer", Tuple.class);
		var res = query.getResultList();
		res.forEach(r->log.info("customer: {} with contract_id: {}", r.get("cust"), r.get("contract_id")));
		assertFalse(res.isEmpty());
		assertTrue(res.size() > 1);
	}
	//******************************************************************************//
	@Test
	void criteriaOrderBySubquery() {
		var query = builder.createQuery(Customer.class);
		var customerRoot = query.from(Customer.class);
		var subquery = query.subquery(Long.class).select(builder.max(customerRoot.get("contracts").get("id")));
		subquery.from(Dual.class);
		query.select(customerRoot).orderBy(builder.desc(subquery));
		var res = entityManager.createQuery(query).getResultList();
		log.info("customer ordered by contract ids in descending order");
		res.forEach(r->log.info("{}", r));
		assertFalse(res.isEmpty());
		assertEquals(res.get(0).getId(), 2L);
	}

	@Test
	void criteriaOrderBySubquery1() {
		var query = builder.createQuery(Customer.class);
		var customerRoot = query.from(Customer.class);
		var subquery = query.subquery(Long.class).select(builder.max(customerRoot.get("contracts").get("createDate")));
		subquery.from(Dual.class);
		query.select(customerRoot).orderBy(builder.desc(subquery));
		var res = entityManager.createQuery(query).getResultList();
		log.info("customer ordered by contract creation date in descending order");
		res.forEach(r->log.info("{}", r));
		assertFalse(res.isEmpty());
		assertEquals(res.get(0).getId(), 2L);
	}
	//******************************************************************************//
	@Test
	void joinCompact() {
		var builder = entityManager.getCriteriaBuilder();
		var query = builder.createQuery(Customer.class);

		Metamodel m = entityManager.getMetamodel();

		EntityType<Customer> Customer_ = m.entity(Customer.class);
		EntityType<Contract> Contract_ = m.entity(Contract.class);

		Root<Customer> root = query.from(Customer.class);
		var join = root.join(Customer_.getList("contracts", Contract.class), JoinType.LEFT);
		//query.select(root).where(builder.equal(root.get("id"), 1L));
		//query.select(root).where(builder.equal(join.get("id"), 2L));

		var subQuery = query.subquery(Long.class).where(builder.equal(join.get("customer"), root.get("id")));
		var rootSub = subQuery.from(Contract.class);
		subQuery.select(builder.max(rootSub.get("id")));

		query.select(root).where(builder.equal(join.get("id"), subQuery));

		var q = entityManager.createQuery(query);
		var res = q.getResultList();
		System.out.println(res);
	}

	@Test
	void includeToSelect() {
		var builder = entityManager.getCriteriaBuilder();
		var query = builder.createQuery(Customer.class);

		Metamodel m = entityManager.getMetamodel();

		EntityType<Customer> Customer_ = m.entity(Customer.class);
		EntityType<Contract> Contract_ = m.entity(Contract.class);

		Root<Customer> root = query.from(Customer.class);
		var join = root.join(Customer_.getList("contracts", Contract.class), JoinType.LEFT);
		//query.select(root).where(builder.equal(root.get("id"), 1L));
		//query.select(root).where(builder.equal(join.get("id"), 2L));

		var subQuery = query.subquery(Long.class).where(builder.equal(join.get("customer"), root.get("id")));
		var rootSub = subQuery.from(Contract.class);
		subQuery.select(builder.max(rootSub.get("id")));

		query.select(root).where(builder.equal(join.get("id"), subQuery));

		var q = entityManager.createQuery(query);
		var res = q.getResultList();
		System.out.println(res);
	}
}
