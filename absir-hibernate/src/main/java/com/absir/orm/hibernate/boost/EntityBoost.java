/**
 * Copyright 2013 ABSir's Studio
 * 
 * All right reserved
 *
 * Create on 2013-3-8 下午12:43:09
 */
package com.absir.orm.hibernate.boost;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Index;

import org.hibernate.MappingException;
import org.hibernate.cfg.Configuration;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.ForeignKey;
import org.hibernate.mapping.OneToMany;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.ToOne;
import org.hibernate.mapping.UniqueKey;
import org.hibernate.mapping.Value;

import com.absir.bean.basis.Environment;
import com.absir.bean.core.BeanFactoryUtils;
import com.absir.core.kernel.KernelClass;
import com.absir.core.kernel.KernelCollection;
import com.absir.core.kernel.KernelObject;
import com.absir.core.kernel.KernelReflect;
import com.absir.core.kernel.KernelString;
import com.absir.orm.hibernate.SessionFactoryBoost;
import com.absir.orm.hibernate.SessionFactoryUtils;
import com.absir.orm.value.JaAssoc;
import com.absir.orm.value.JaColum;
import com.absir.orm.value.JaEntity;
import com.absir.orm.value.JaProxy;
import com.absir.orm.value.JaSubTable;
import com.absir.orm.value.JaTable;

/**
 * @author absir
 * 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class EntityBoost {

	/**
	 * @param configuration
	 * @param sessionFactoryBoost
	 * @param locale
	 */
	public static void boost(Configuration configuration, SessionFactoryBoost sessionFactoryBoost, boolean locale) {
		Map<String, PersistentClass> classes = (Map<String, PersistentClass>) KernelObject.declaredGet(configuration, "classes");
		boostEntityTable(classes);
		Map<String, Table> tables = (Map<String, Table>) KernelObject.declaredGet(configuration, "tables");
		assocEntityPersistent(classes, tables, configuration.getImports(), sessionFactoryBoost);
	}

	/**
	 * @param classes
	 */
	private static void boostEntityTable(Map<String, PersistentClass> classes) {
		Map<String, Column> tableColumns = new HashMap<String, Column>();
		Map<String, List<Column>> subTableColumns = new HashMap<String, List<Column>>();
		Map<String, List<org.hibernate.mapping.Index>> tableIndexs = new HashMap<String, List<org.hibernate.mapping.Index>>();
		for (Iterator<Entry<String, PersistentClass>> iterator = classes.entrySet().iterator(); iterator.hasNext();) {
			PersistentClass persistentClass = iterator.next().getValue();
			Class<?> mappedClass = persistentClass.getMappedClass();
			if (mappedClass == null) {
				continue;
			}

			String tableName = persistentClass.getTable().getName();
			int tableType = 0;
			if (mappedClass.getAnnotation(JaTable.class) != null) {
				tableType = 1;

			} else if (mappedClass.getAnnotation(JaSubTable.class) != null) {
				tableType = 2;
			}

			for (Iterator<Property> propertyIt = persistentClass.getPropertyClosureIterator(); propertyIt.hasNext();) {
				Property property = propertyIt.next();
				Field field = KernelReflect.declaredField(property.getPersistentClass().getMappedClass(), property.getName());
				if (field == null) {
					continue;
				}

				// 字段定义
				JaColum jaColum = field.getAnnotation(JaColum.class);

				// 联合索引
				if (jaColum != null) {
					// 外键定义
					if (!jaColum.foreignKey()) {
						Value value = property.getValue();
						Table table = null;
						if (value instanceof OneToMany) {
							table = value.getTable();

						} else if (value instanceof Collection) {
							table = ((Collection) value).getCollectionTable();
						}

						if (table != null) {
							for (Iterator foreignKeyIterator = table.getForeignKeyIterator(); foreignKeyIterator.hasNext();) {
								foreignKeyIterator.next();
								foreignKeyIterator.remove();
							}

						} else if (value instanceof ToOne) {
							List columns = ((ToOne) value).getConstraintColumns();
							if (columns != null) {
								for (Iterator foreignKeyIterator = value.getTable().getForeignKeyIterator(); foreignKeyIterator.hasNext();) {
									ForeignKey foreignKey = (ForeignKey) foreignKeyIterator.next();
									if (KernelCollection.equals(columns, foreignKey.getColumns())) {
										foreignKeyIterator.remove();
										break;
									}
								}
							}
						}

					} else if (jaColum.indexs().length > 0) {
						Table table = persistentClass.getTable();
						Map<String, Column> columns = (Map<String, Column>) KernelObject.declaredGet(table, "columns");
						for (Index index : jaColum.indexs()) {
							String[] columnNames = KernelString.isEmpty(index.columnList()) ? new String[] { index.name() } : index.columnList().split(",");
							if (index.unique()) {
								UniqueKey uniqueKey = new UniqueKey();
								uniqueKey.setName(index.name());
								for (String columnName : columnNames) {
									Column col = columns.get(columnName);
									if (col != null) {
										uniqueKey.addColumn(col);
									}
								}

								if (uniqueKey.getColumnSpan() > 0) {
									if (KernelString.isEmpty(uniqueKey.getName())) {
										uniqueKey.setName(property.getName());
									}

									uniqueKey.setTable(table);
									table.addUniqueKey(uniqueKey);
								}

							} else {
								org.hibernate.mapping.Index mappingIndex = new org.hibernate.mapping.Index();
								mappingIndex.setName(index.name());
								for (String columnName : columnNames) {
									Column col = columns.get(columnName);
									if (col != null) {
										mappingIndex.addColumn(col);
									}
								}

								if (mappingIndex.getColumnSpan() > 0) {
									if (KernelString.isEmpty(mappingIndex.getName())) {
										mappingIndex.setName(property.getName());
									}

									mappingIndex.setTable(table);
									table.addIndex(mappingIndex);
								}
							}
						}
					}
				}

				// 多种索引合并
				if (tableType != 0) {
					Iterator<org.hibernate.mapping.Index> it = persistentClass.getTable().getIndexIterator();
					if (it != null && it.hasNext()) {
						List<org.hibernate.mapping.Index> indexs = tableIndexs.get(tableName);
						if (indexs == null) {
							indexs = new ArrayList<org.hibernate.mapping.Index>();
							tableIndexs.put(tableName, indexs);

						} else {
							for (org.hibernate.mapping.Index index : indexs) {
								persistentClass.getTable().addIndex(index);
							}
						}

						while (it.hasNext()) {
							indexs.add(it.next());
						}
					}
				}

				// 单一字段详细定义
				if (property.getColumnSpan() != 1) {
					continue;
				}

				Column column = (Column) property.getColumnIterator().next();
				if (jaColum != null) {
					if (!"".equals(jaColum.comment())) {
						column.setComment(jaColum.comment());
					}

					if (!"`".equals(jaColum.defaultValue())) {
						column.setDefaultValue(jaColum.defaultValue());
					}

					if (!"".equals(jaColum.sqlType())) {
						column.setSqlType(jaColum.sqlType());
					}

					if (jaColum.length() > 0) {
						column.setLength(jaColum.length());
					}
				}

				if (tableType == 0) {
					continue;
				}

				String columnName = tableName + '`' + column.getName();
				if (tableType == 1) {
					tableColumns.put(columnName, column);
					List<Column> cols = subTableColumns.get(columnName);
					if (cols != null) {
						for (Column col : cols) {
							col.setComment(column.getComment());
							col.setDefaultValue(column.getDefaultValue());
							col.setSqlType(column.getSqlType());
							col.setLength(column.getLength());
						}
					}

					subTableColumns.remove(columnName);

				} else if (tableType == 2) {
					Column col = tableColumns.get(columnName);
					if (col == null) {
						List<Column> columns = subTableColumns.get(columnName);
						if (columns == null) {
							columns = new ArrayList<Column>();
							subTableColumns.put(columnName, columns);
						}

						columns.add(column);

					} else {
						column.setComment(col.getComment());
						column.setDefaultValue(col.getDefaultValue());
						column.setSqlType(col.getSqlType());
						column.setLength(col.getLength());
					}
				}
			}
		}
	}

	/**
	 * @param classes
	 * @param tables
	 */
	private static void assocEntityPersistent(Map<String, PersistentClass> classes, Map<String, Table> tables, Map<String, String> imports, SessionFactoryBoost sessionFactoryBoost) {
		List<PersistentClass> persistentClasses = new ArrayList<PersistentClass>();
		List<ImplementPersistent> implementPersistents = new ArrayList<ImplementPersistent>();
		for (Entry<String, PersistentClass> entry : classes.entrySet()) {
			PersistentClass persistentClass = entry.getValue();
			Class<?> mappedClass = persistentClass.getMappedClass();
			if (mappedClass == null) {
				continue;
			}

			if (mappedClass.getPackage().getAnnotation(JaProxy.class) != null) {
				try {
					Class implementClass = Class.forName(KernelClass.parentName(mappedClass));
					implementPersistents.add(new ImplementPersistent(implementClass, persistentClass));

				} catch (MappingException e) {
				} catch (ClassNotFoundException e) {
				}

				for (Iterator iterator = persistentClass.getReferenceablePropertyIterator(); iterator.hasNext();) {
					Property property = (Property) iterator.next();

					if (property.getValue() instanceof Collection) {
						Collection collection = (Collection) property.getValue();
						if (collection.getCollectionTable() != null) {
							removeTable(collection.getCollectionTable(), tables);
						}
					}
				}

				removeTable(persistentClass.getTable(), tables);

			} else {
				persistentClasses.add(persistentClass);
			}
		}

		for (PersistentClass persistentClass : persistentClasses) {
			JpaEntityPersistent jpaEntityPersistent = new JpaEntityPersistent(persistentClass.getJpaEntityName(), persistentClass.getEntityName(), persistentClass.getTable().getName(),
					persistentClass.getMappedClass());
			assocEntityPersistent(jpaEntityPersistent, implementPersistents, classes, tables, imports, SessionFactoryUtils.get().getAssocDepth(), sessionFactoryBoost);
		}
	}

	/**
	 * @param table
	 * @param tables
	 */
	private static void removeTable(Table table, Map<String, Table> tables) {
		tables.remove(table.getName());
		KernelObject.declaredSet(table, "foreignKeys", new HashMap());
	}

	/**
	 * @param jpaEntityPersistent
	 * @param implementPersistents
	 * @param classes
	 * @param tables
	 * @param imports
	 * @param associate
	 * @param sessionFactoryBoost
	 */
	private static void assocEntityPersistent(JpaEntityPersistent jpaEntityPersistent, List<ImplementPersistent> implementPersistents, Map<String, PersistentClass> classes, Map<String, Table> tables,
			Map<String, String> imports, int associate, SessionFactoryBoost sessionFactoryBoost) {
		if (--associate < 0) {
			return;
		}

		Class<?> entityClass = jpaEntityPersistent.entityClass;
		JaEntity jaEntity = entityClass.getAnnotation(JaEntity.class);
		if (jaEntity != null) {
			for (JaAssoc jaAssoc : jaEntity.jaAssoces()) {
				PersistentClass persistentClass = classes.get(jaAssoc.entityClass().getName());
				if (persistentClass != null) {
					JpaEntityPersistent assocJpaEntityPersistent = assocEntityPersistent(jaAssoc.entityName(), jaAssoc.tableName(), persistentClass, jpaEntityPersistent, implementPersistents,
							classes, tables, imports, associate, sessionFactoryBoost);
					EntityAssoc.addPersistentClass(jpaEntityPersistent.entityName, assocJpaEntityPersistent.entityName, jaAssoc);
				}
			}
		}

		Class superEntityClass = entityClass;
		while (superEntityClass != null && superEntityClass != Object.class) {
			for (Class implClass : superEntityClass.getInterfaces()) {
				implementPersistent(implClass, jpaEntityPersistent, implementPersistents, classes, tables, imports, associate, sessionFactoryBoost);
			}

			superEntityClass = superEntityClass.getSuperclass();
			implementPersistent(superEntityClass, jpaEntityPersistent, implementPersistents, classes, tables, imports, associate, sessionFactoryBoost);
		}

		EntityAssoc.addPersistentClasses(jpaEntityPersistent.entityName, jpaEntityPersistent.jpaEntityName, jaEntity, classes, sessionFactoryBoost);
	}

	/**
	 * @param entityName
	 * @param tableName
	 * @param persistentClass
	 * @param jpaEntityPersistent
	 * @param implementPersistents
	 * @param classes
	 * @param tables
	 * @param imports
	 * @param associate
	 * @param sessionFactoryBoost
	 * @return
	 */
	private static JpaEntityPersistent assocEntityPersistent(String entityName, String tableName, PersistentClass persistentClass, JpaEntityPersistent jpaEntityPersistent,
			List<ImplementPersistent> implementPersistents, Map<String, PersistentClass> classes, Map<String, Table> tables, Map<String, String> imports, int associate,
			SessionFactoryBoost sessionFactoryBoost) {
		if (entityName.isEmpty()) {
			entityName = persistentClass.getMappedClass().getSimpleName();
		}

		if (tableName.isEmpty()) {
			tableName = entityName.toLowerCase();
		}

		String jpaEntityName = jpaEntityPersistent.jpaEntityName + entityName;
		entityName = jpaEntityPersistent.entityName + entityName;
		tableName = jpaEntityPersistent.tableName + '_' + tableName;
		assocEntityPersistent(jpaEntityName, entityName, tableName, persistentClass, classes, tables, imports, sessionFactoryBoost);
		jpaEntityPersistent = new JpaEntityPersistent(jpaEntityName, entityName, tableName, persistentClass.getMappedClass());
		assocEntityPersistent(jpaEntityPersistent, implementPersistents, classes, tables, imports, associate, sessionFactoryBoost);
		return jpaEntityPersistent;
	}

	/**
	 * @param jpaEntityName
	 * @param entityName
	 * @param tableName
	 * @param persistentClass
	 * @param classes
	 * @param tables
	 */
	private static void assocEntityPersistent(String jpaEntityName, String entityName, String tableName, PersistentClass persistentClass, Map<String, PersistentClass> classes,
			Map<String, Table> tables, Map<String, String> imports, SessionFactoryBoost sessionFactoryBoost) {
		if (!tables.containsKey(tableName)) {
			Table table = KernelObject.clone(persistentClass.getTable());
			table.setName(tableName);
			tables.put(tableName, table);
			if (!classes.containsKey(entityName)) {
				persistentClass = KernelObject.clone(persistentClass);
				KernelObject.declaredSet(persistentClass, "table", table);
				persistentClass.setEntityName(entityName);
				persistentClass.setJpaEntityName(jpaEntityName);
				classes.put(entityName, persistentClass);
				persistentClass = KernelObject.clone(persistentClass);
				imports.put(jpaEntityName, entityName);
				for (Iterator iterator = persistentClass.getReferenceablePropertyIterator(); iterator.hasNext();) {
					Property property = (Property) iterator.next();
					if (property.getValue() instanceof Collection) {
						Collection collection = (Collection) property.getValue();

						String collectionTableName = null;
						if (collection.getCollectionTable() != null) {
							if (collection.getElement() instanceof SimpleValue) {
								SimpleValue value = (SimpleValue) collection.getElement();
								if (value.getConstraintColumns() != null && value.getConstraintColumns().size() == 1) {
									collectionTableName = tableName + "_" + ((Column) value.getConstraintColumns().get(0)).getName();
								}
							}

							if (collectionTableName == null) {
								collectionTableName = tableName + collection.getNodeName();
							}

							Table collectionTable = tables.get(collectionTableName);
							if (collectionTable == null) {
								collectionTable = KernelObject.clone(collection.getCollectionTable());
								collectionTable.setName(collectionTableName);
								tables.put(collectionTableName, collectionTable);
							}

							collection.setCollectionTable(collectionTable);
						}
					}
				}

				if (BeanFactoryUtils.getEnvironment().compareTo(Environment.DEBUG) <= 0) {
					System.out.println("Add assoc entity name:" + persistentClass.getEntityName() + " table:" + persistentClass.getTable().getName() + " jpa:" + persistentClass.getJpaEntityName());
				}
			}
		}
	}

	/**
	 * @param implementClass
	 * @param jpaEntityPersistent
	 * @param implementPersistents
	 * @param classes
	 * @param tables
	 * @param imports
	 * @param associate
	 * @param sessionFactoryBoost
	 */
	private static void implementPersistent(Class implementClass, JpaEntityPersistent jpaEntityPersistent, List<ImplementPersistent> implementPersistents, Map<String, PersistentClass> classes,
			Map<String, Table> tables, Map<String, String> imports, int associate, SessionFactoryBoost sessionFactoryBoost) {
		for (ImplementPersistent implementPersistent : implementPersistents) {
			if (implementClass == implementPersistent.implementClass) {
				assocEntityPersistent(implementPersistent.entityName, implementPersistent.tableName, implementPersistent.persistentClass, jpaEntityPersistent, implementPersistents, classes, tables,
						imports, associate, sessionFactoryBoost);
			}
		}
	}

	/**
	 * @author absir
	 * 
	 */
	private static class ImplementPersistent {

		/** implementClass */
		private Class implementClass;

		/** entityName */
		private String entityName;

		/** tableName */
		private String tableName;

		/** persistentClass */
		private PersistentClass persistentClass;

		/**
		 * @param implementClass
		 * @param persistentClass
		 */
		private ImplementPersistent(Class implementClass, PersistentClass persistentClass) {
			this.implementClass = implementClass;
			Class<?> mappedClass = persistentClass.getMappedClass();
			javax.persistence.Entity entity = mappedClass.getAnnotation(javax.persistence.Entity.class);
			this.entityName = (entity == null || entity.name().isEmpty()) ? mappedClass.getSimpleName() : entity.name();
			javax.persistence.Table table = mappedClass.getAnnotation(javax.persistence.Table.class);
			this.tableName = (table == null || table.name().isEmpty()) ? entityName.toLowerCase() : table.name();
			this.persistentClass = persistentClass;
		}
	}

	/**
	 * @author absir
	 * 
	 */
	private static class JpaEntityPersistent {

		/** jpaEntityName */
		private String jpaEntityName;

		/** entityName */
		private String entityName;

		/** tableName */
		private String tableName;

		/** entityClass */
		private Class entityClass;

		/**
		 * @param jpaEntityName
		 * @param entityName
		 * @param tableName
		 * @param entityClass
		 */
		private JpaEntityPersistent(String jpaEntityName, String entityName, String tableName, Class entityClass) {
			this.jpaEntityName = jpaEntityName;
			this.entityName = entityName;
			this.tableName = tableName;
			this.entityClass = entityClass;
		}
	}
}
