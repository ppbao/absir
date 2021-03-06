/**
 * Copyright 2014 ABSir's Studio
 * 
 * All right reserved
 *
 * Create on 2014-3-3 上午11:04:41
 */
package com.absir.orm.hibernate.transaction;

import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.context.spi.CurrentSessionContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;

import com.absir.core.kernel.KernelClass;
import com.absir.orm.transaction.ISessionContext;
import com.absir.orm.transaction.ISessionHolder;
import com.absir.orm.transaction.TransactionAttribute;
import com.absir.orm.transaction.TransactionContext;
import com.absir.orm.transaction.TransactionHolder;
import com.absir.orm.transaction.TransactionSession;

/**
 * @author absir
 * 
 */
@SuppressWarnings({ "rawtypes", "serial" })
public class JSessionContext implements CurrentSessionContext, ISessionContext {

	/** sessionFactory */
	private SessionFactoryImplementor sessionFactory;

	/** transactionContext */
	private JTransactionContext transactionContext;

	/**
	 * @param sessionFactory
	 */
	public JSessionContext(SessionFactoryImplementor sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hibernate.context.spi.CurrentSessionContext#currentSession()
	 */
	@Override
	public Session currentSession() throws HibernateException {
		// TODO Auto-generated method stub
		if (transactionContext == null) {
			throw new HibernateException("No TransactionContext configured!");
		}

		JTransactionSession transactionSession = transactionContext.getTransactionSession();
		if (transactionSession == null) {
			throw new HibernateException("No transactionSession configured!");
		}

		transactionSession.open(this);
		JSession session = transactionSession.getCurrentSession();
		return session == null ? null : session.getSession();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.absir.orm.transaction.ISessionContext#get(java.lang.String)
	 */
	@Override
	public TransactionContext get(String name) {
		// TODO Auto-generated method stub
		if (transactionContext == null) {
			transactionContext = new JTransactionContext(name);
		}

		return transactionContext;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.absir.orm.transaction.ISessionContext#open(com.absir.orm.transaction
	 * .ISessionHolder, com.absir.orm.transaction.TransactionAttribute,
	 * com.absir.orm.transaction.TransactionSession)
	 */
	@Override
	public ISessionHolder open(ISessionHolder sessionHolder, TransactionAttribute transactionAttribute, TransactionSession transactionSession) {
		// TODO Auto-generated method stub
		return open(sessionFactory, sessionHolder, transactionAttribute, (JTransactionSession) transactionSession);
	}

	/**
	 * @param sessionFactory
	 * @param transactionAttributeBefore
	 * @param transactionAttribute
	 * @param jTransactionSession
	 * @return
	 */
	public static ISessionHolder open(SessionFactory sessionFactory, ISessionHolder sessionHolder, TransactionAttribute transactionAttribute, final JTransactionSession jTransactionSession) {
		TransactionHolder transactionHolder = new TransactionHolder(sessionHolder, transactionAttribute) {

			@Override
			public void close(Throwable e) {
				// TODO Auto-generated method stub
				boolean nested = (flag & TransactionHolder.NESTED_FLAG) != 0;
				JSession jSession = nested ? jTransactionSession.closeCurrentSession() : jTransactionSession.getCurrentSession();
				if (jSession != null) {
					if (nested) {
						try {
							if (!isReadOnly()) {
								Transaction transaction = jSession.getTransaction();
								if (transaction != null) {
									if (e == null || rollback == null || !KernelClass.isAssignableFrom(rollback, e.getClass())) {
										transaction.commit();

									} else {
										transaction.rollback();
									}

								} else if (!isRequired()) {
									jSession.getSession().flush();
								}
							}

						} finally {
							// close session
							jSession.getSession().close();
						}

					} else {
						if ((flag & READONLY_EQ_FLAG) == 0) {
							if (isReadOnly()) {
								jSession.getSession().setFlushMode(FlushMode.COMMIT);
								jSession.getSession().setDefaultReadOnly(false);

							} else {
								jSession.getSession().setFlushMode(FlushMode.MANUAL);
								jSession.getSession().setDefaultReadOnly(true);
							}
						}

						if (timeout != 0) {
							jSession.getTransaction().setTimeout((int) timeout);
						}
					}
				}
			}
		};

		int flag = transactionHolder.getFlag();
		boolean nested = (flag & TransactionHolder.NESTED_FLAG) != 0;
		JSession jSession;
		if (nested) {
			jSession = new JSession(sessionFactory.openSession());
			jTransactionSession.openCurrentSession(jSession);

		} else {
			jSession = jTransactionSession.getCurrentSession();
		}

		if (nested || (flag & TransactionHolder.READONLY_EQ_FLAG) == 0) {
			if (transactionHolder.isReadOnly()) {
				jSession.getSession().setFlushMode(FlushMode.MANUAL);
				jSession.getSession().setDefaultReadOnly(true);

			} else {
				jSession.getSession().setFlushMode(FlushMode.COMMIT);
				jSession.getSession().setDefaultReadOnly(false);
			}
		}

		if (nested || (flag & TransactionHolder.REQUIRED_EQ_FLAG) == 0) {
			if (transactionHolder.isRequired() || !transactionHolder.isReadOnly()) {
				Transaction transaction = jSession.openTransaction();
				int timeout = transactionAttribute.getTimeout();
				if (timeout > 0) {
					transaction.setTimeout(timeout);
				}
			}
		}

		return transactionHolder;
	}
}
