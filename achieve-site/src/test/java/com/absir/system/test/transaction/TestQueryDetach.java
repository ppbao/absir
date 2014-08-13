/**
 * Copyright 2014 ABSir's Studio
 * 
 * All right reserved
 *
 * Create on 2014-4-28 下午3:57:16
 */
package com.absir.system.test.transaction;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.junit.Test;

import com.absir.appserv.client.bean.JFollowMessage;
import com.absir.appserv.data.value.DataQuery;
import com.absir.appserv.data.value.FirstResults;
import com.absir.appserv.data.value.MaxResults;
import com.absir.appserv.jdbc.JdbcPage;
import com.absir.appserv.system.bean.JUser;
import com.absir.appserv.system.dao.BeanDao;
import com.absir.appserv.system.dao.utils.QueryDaoUtils;
import com.absir.appserv.system.helper.HelperJson;
import com.absir.async.value.Async;
import com.absir.bean.core.BeanFactoryUtils;
import com.absir.bean.inject.value.Bean;
import com.absir.orm.hibernate.SessionFactoryUtils;
import com.absir.orm.transaction.value.Transaction;
import com.absir.system.test.AbstractTestInject;

/**
 * @author absir
 * 
 */
public class TestQueryDetach extends AbstractTestInject {

	@Bean
	public static abstract class TestAsync {

		/** ME */
		protected static final TestAsync ME = BeanFactoryUtils.get(TestAsync.class);

		@Async
		public void test() {
			Session session = SessionFactoryUtils.get().getSessionFactory().openSession();
			session.close();
		}
	}

	@Bean
	@Transaction
	public static abstract class TestService {

		/** ME */
		protected static final TestService ME = BeanFactoryUtils.get(TestService.class);

		@DataQuery("SELECT o.id FROM JPlayer o WHERE o.id IN :p0")
		protected abstract Long[] getPlayerIds(long[] playerIds);
		
		@DataQuery("SELECT o FROM JUser o WHERE o.username LIKE :p0")
		protected abstract JUser findByUsername(String username);

		@DataQuery("SELECT o FROM JUser o WHERE o.username LIKE ?")
		protected abstract List<JUser> findByUsernames(String username);

		@DataQuery("SELECT o FROM JUser o WHERE o.username LIKE ?")
		protected abstract List<JUser> findByUsernames(String username, @FirstResults int first, @MaxResults int max);

		@DataQuery("SELECT o FROM JUser o WHERE o.username LIKE ?")
		protected abstract List<JUser> findByUsernamePage(String username, JdbcPage page);

		@DataQuery(value = "SELECT o FROM JUser o WHERE o.username LIKE %?%")
		protected abstract List<Map<String, Object>> testError();

		@DataQuery(value = "show full processlist", nativeQuery = true, aliasType = Map.class)
		protected abstract List<Map<String, Object>> showProcesslist();

		/**
		 * @param obj
		 */
		protected void println(Object obj) {
			// System.out.println(obj);
			System.out.println(HelperJson.encodeNull(obj));
		}

		/**
		 * 
		 */
		@Transaction
		public void test() {
			println(findByUsername("absir"));
			println(findByUsernames("absir"));
			println(findByUsernames("%a%", 1, 5));
			JdbcPage page = new JdbcPage();
			println(findByUsernamePage("%a%", page));
			println(page);

			JUser user = new JUser();
			user.setUsername("dssdsddd1444");
			BeanDao.getSession().persist(user);

			user = new JUser();
			user.setUsername("dssdsddd123444");
			BeanDao.getSession().persist(user);
		}

		@Transaction
		public void test1() {
			QueryDaoUtils.createQueryArray(BeanDao.getSession(), "SELECT o FROM JFollowMessage o").list();
		}

		@Transaction
		public void test2() {
			@SuppressWarnings("unchecked")
			Iterator<Object> it = QueryDaoUtils.createQueryArray(BeanDao.getSession(), "SELECT o FROM JFollowMessage o").iterate();
			while (it.hasNext()) {
				it.next();
			}
		}

		@Transaction
		public void test3() {
			@SuppressWarnings("unchecked")
			Iterator<Object> it = QueryDaoUtils.createQueryArray(BeanDao.getSession(), "SELECT o.id FROM JFollowMessage o").setCacheable(true).iterate();
			while (it.hasNext()) {
				JFollowMessage message = BeanDao.get(BeanDao.getSession(), JFollowMessage.class, (Serializable) it.next());
				System.out.println(message);
			}
		}

		@Async
		protected void testError2() throws InterruptedException {
			Session session = BeanDao.getSession();
			session.close();
		}
	}

	@Test
	public void test() throws InterruptedException {
		System.out.println( TestService.ME.getPlayerIds(new long[]{1,2,3}));
		TestService.ME.test();
		
		Thread.sleep(3000);

		for (Object obj : TestService.ME.showProcesslist()) {
			System.out.println(HelperJson.encodeNull(obj));
		}

		System.out.println("连接数量1:" + TestService.ME.showProcesslist().size());
		for (int i = 0; i < 100; i++) {
			try {
				TestService.ME.testError();
			} catch (Exception e) {
				// TODO: handle exception
			}

		}

		int size = TestService.ME.showProcesslist().size();
		System.out.println("开连接数量2:" + size);

		for (int i = 0; i < 5; i++) {
			try {
				TestService.ME.testError2();
				// TestAsync.ME.test();

			} catch (Exception e) {
				// TODO: handle exception
			}
		}

		// System.out.println("连接数量3:" +
		// TestService.ME.showProcesslist().size());

		Thread.sleep(20000);
		System.out.println("连接数量4:" + TestService.ME.showProcesslist().size());

		for (int i = 0; i < 10; i++) {
			Thread.sleep(1000);
			System.out.println("连接数量" + (i + 5) + ":" + TestService.ME.showProcesslist().size());
		}

		// // 测试1
		// System.out.println("测试1");
		// TestService.ME.test1();
		// // 测试2
		// System.out.println("测试2");
		// TestService.ME.test2();
		// // 测试3
		// System.out.println("测试3");
		// TestService.ME.test3();
		// System.out.println("测试4");
		// TestService.ME.test3();

		// TestService.ME.test();
		/*
		 * SessionFactory sessionFactory =
		 * SessionFactoryUtils.get().getSessionFactory(); Session session =
		 * sessionFactory.openSession(); session.setFlushMode(FlushMode.AUTO);
		 * try { JUser user = new JUser(); user.setUsername("dssdsddd1444");
		 * session.persist(user);
		 * 
		 * } finally {
		 * 
		 * session.beginTransaction().commit();
		 * 
		 * session.flush(); session.close(); }
		 */
	}
}