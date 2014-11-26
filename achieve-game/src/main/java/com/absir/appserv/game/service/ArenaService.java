/**
 * Copyright 2014 ABSir's Studio
 * 
 * All right reserved
 *
 * Create on 2014年11月25日 下午1:06:14
 */
package com.absir.appserv.game.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.absir.appserv.data.value.DataQuery;
import com.absir.appserv.data.value.MaxResults;
import com.absir.appserv.game.bean.JbPlayer;
import com.absir.appserv.game.context.PlayerServiceBase;
import com.absir.bean.basis.Base;
import com.absir.bean.core.BeanFactoryUtils;
import com.absir.bean.inject.value.Bean;
import com.absir.bean.inject.value.InjectOrder;
import com.absir.bean.inject.value.Stopping;
import com.absir.context.schedule.value.Schedule;
import com.absir.core.kernel.KernelLang.CallbackTemplate;
import com.absir.orm.transaction.value.Transaction;

/**
 * @author absir
 *
 */
@Base
@Bean
public abstract class ArenaService {

	/** ME */
	public static final ArenaService ME = BeanFactoryUtils.get(ArenaService.class);

	/** idMapArenaBase */
	private Map<Long, ArenaBase> idMapArenaBase = new ConcurrentHashMap<Long, ArenaService.ArenaBase>();

	/**
	 * 最后一名
	 * 
	 * @param serverId
	 * @return
	 */
	@Transaction(readOnly = true)
	@DataQuery(value = "SELECT MAX(o.arena) FROM JPlayer o WHERE o.serverId = ?")
	public abstract int maxArena(Long serverId);

	/**
	 * 查询排名
	 * 
	 * @param serverId
	 * @param minArena
	 * @param maxArena
	 * @return
	 */
	@Transaction(readOnly = true)
	@DataQuery(value = "SELECT o.id, o.arena FROM JPlayer o WHERE o.serverId = ? AND o.arena >= ?", aliasType = List.class)
	public abstract List<List<Object>> listArenas(Long serverId, Integer minArena, @MaxResults Integer maxArena);

	/**
	 * 查询排名
	 * 
	 * @param serverId
	 * @param minArena
	 * @param maxArena
	 * @return
	 */
	@Transaction(readOnly = true)
	@DataQuery(value = "SELECT o.id, o.arena FROM JPlayer o WHERE o.serverId = ? AND o.arena >= ? AND o.arena < ?", aliasType = List.class)
	public abstract List<List<Object>> findIdArenas(Long serverId, Integer minArena, Integer maxArena);

	/**
	 * 
	 * @return
	 */
	protected ArenaBase createArenaBase() {
		return new ArenaBase();
	}

	/**
	 * @param serverId
	 * @return
	 */
	public ArenaBase getArenaBase(Long serverId) {
		ArenaBase arenaBase = idMapArenaBase.get(serverId);
		if (arenaBase == null) {
			synchronized (idMapArenaBase) {
				arenaBase = idMapArenaBase.get(serverId);
				if (arenaBase == null) {
					arenaBase = createArenaBase();
					arenaBase.serverId = serverId;
				}

				idMapArenaBase.put(serverId, arenaBase);
			}
		}

		return arenaBase;
	}

	/**
	 * 保存排名
	 */
	@Transaction
	@InjectOrder(value = -1)
	@Schedule(fixedDelay = 6 * 3600000)
	@Stopping
	public void saveArenas() {
		for (ArenaBase arenaBase : idMapArenaBase.values()) {
			arenaBase.save();
		}
	}

	/**
	 * 竞技场对象
	 * 
	 * @author absir
	 *
	 */
	public static class ArenaBase {

		/** serverId */
		protected Long serverId;

		/** maxArena */
		protected int maxArena;

		/** arenaIds */
		protected Map<Integer, Long> arenaIds = new HashMap<Integer, Long>();

		/** idArenas */
		protected Map<Long, Integer> idArenas = new HashMap<Long, Integer>();

		/**
		 * 载入查询
		 * 
		 * @param idArenas
		 */
		protected void load(List<List<Object>> arenas) {
			for (List<Object> ids : arenas) {
				Integer arn = (Integer) ids.get(1);
				if (!arenaIds.containsKey(arn)) {
					Long id = (Long) ids.get(0);
					if (arn > maxArena) {
						maxArena = arn;
					}

					arenaIds.put(arn, id);
				}
			}
		}

		/**
		 * 排名列表
		 * 
		 * @param arena
		 * @param arenaCount
		 * @return
		 */
		public List<Long> list(int arena, int arenaCount) {
			int max = maxArena;
			Map<Integer, Long> arnIds = arenaIds;
			List<Long> finds = new ArrayList<Long>();
			boolean loaded = false;
			for (; arena < max; arena++) {
				Long id = arnIds.get(arena);
				if (id == null) {
					if (!loaded) {
						loaded = true;
						List<List<Object>> arenas = ArenaService.ME.listArenas(serverId, arena, arenaCount);
						synchronized (this) {
							load(arenas);
							max = maxArena;
							arnIds = arenaIds;
						}

						id = arnIds.get(arena);
					}
				}

				if (id != null) {
					finds.add(id);
					arenaCount--;
				}

				arena++;
			}

			return finds;
		}

		/**
		 * 实时排名
		 * 
		 * @param player
		 */
		public void analyze(JbPlayer player) {
			Long id = player.getId();
			Integer arena = idArenas.get(id);
			if (arena == null) {
				int arn = player.getArena();
				if (arn <= 0) {
					arn = ArenaService.ME.maxArena(serverId) + 1;
					setArena(id, arn);
					player.setArena(arn);
				}

			} else {
				player.setArena(arena);
			}
		}

		/**
		 * 设置排名
		 * 
		 * @param id
		 * @param arena
		 */
		protected void setArena(Long id, Integer arena) {
			Integer arn = idArenas.put(id, arena);
			if (arn != null) {
				if (arn.equals(arena)) {
					return;
				}

				Long oid = arenaIds.get(arn);
				if (oid != null && !oid.equals(id)) {
					arenaIds.remove(arn);
				}
			}

			arenaIds.put(arena, id);
		}

		/**
		 * 交换排名
		 * 
		 * @param player
		 * @param target
		 */
		public synchronized void exchange(JbPlayer player, JbPlayer target) {
			analyze(player);
			analyze(target);
			int arena = player.getArena();
			int targetArena = target.getArena();
			if (arena < targetArena) {
				Long id = player.getId();
				setArena(id, targetArena);
				if (targetArena > maxArena) {
					maxArena = targetArena;
				}

				boolean loaded = false;
				for (arena++; arena < targetArena; arena++) {
					id = arenaIds.get(arena);
					if (id == null) {
						if (loaded) {
							continue;
						}

						loaded = true;
						load(ArenaService.ME.findIdArenas(id, arena, targetArena));
						id = arenaIds.get(arena);
						if (id == null) {
							continue;
						}
					}

					setArena(id, arena - 1);
				}
			}
		}

		/**
		 * 保存排名数据
		 */
		public void save() {
			Map<Long, Integer> idArns = idArenas;
			synchronized (this) {
				arenaIds = new HashMap<Integer, Long>(arenaIds);
				idArenas = new HashMap<Long, Integer>(idArenas);
			}

			for (final Entry<Long, Integer> entry : idArns.entrySet()) {
				PlayerServiceBase.ME.modifyPlayer(entry.getKey(), new CallbackTemplate<JbPlayer>() {

					@Override
					public void doWith(JbPlayer template) {
						// TODO Auto-generated method stub
						template.setArena(entry.getValue());
					}
				});
			}

			synchronized (this) {
				maxArena = 0;
				Iterator<Entry<Long, Integer>> iterator = idArenas.entrySet().iterator();
				while (iterator.hasNext()) {
					Entry<Long, Integer> entry = iterator.next();
					Integer arn = idArns.get(entry.getKey());
					if (arn == null || !arn.equals(entry.getValue())) {
						int arenar = entry.getValue();
						if (arenar > maxArena) {
							maxArena = arenar;
						}

					} else {
						iterator.remove();
					}
				}
			}
		}
	}

}
