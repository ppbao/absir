/**
 * Copyright 2014 ABSir's Studio
 * 
 * All right reserved
 *
 * Create on 2014年10月20日 下午4:21:15
 */
package com.absir.appserv.game.context;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.absir.appserv.configure.JConfigureBase;
import com.absir.appserv.configure.JConfigureUtils;
import com.absir.appserv.configure.conf.ConfigureUtils;
import com.absir.appserv.configure.xls.XlsBase;
import com.absir.appserv.configure.xls.XlsDao;
import com.absir.appserv.configure.xls.XlsUtils;
import com.absir.appserv.game.bean.JbCard;
import com.absir.appserv.game.bean.JbFriend;
import com.absir.appserv.game.bean.JbPlayer;
import com.absir.appserv.game.bean.JbPlayerA;
import com.absir.appserv.game.bean.value.ICardDefine;
import com.absir.appserv.game.bean.value.IPlayerDefine;
import com.absir.appserv.game.bean.value.IPropDefine;
import com.absir.appserv.game.bean.value.IRewardDefine;
import com.absir.appserv.game.bean.value.ITaskDefine;
import com.absir.appserv.game.confiure.JPlayerConfigure;
import com.absir.appserv.game.utils.GameUtils;
import com.absir.appserv.game.value.IExp;
import com.absir.appserv.game.value.LevelExpCxt;
import com.absir.bean.core.BeanFactoryUtils;
import com.absir.bean.inject.value.Inject;
import com.absir.context.core.ContextUtils;
import com.absir.core.dyna.DynaBinder;
import com.absir.core.kernel.KernelClass;

/**
 * @author absir
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class PlayerComponentBase<C extends JbCard, P extends JbPlayerContext<C, ?, ?, ?, ?, ?, ?>, PC extends JPlayerConfigure, PD extends IPlayerDefine, CD extends ICardDefine, CE extends IExp, PP extends IPropDefine, RD extends IRewardDefine, TD extends ITaskDefine> {

	// 角色上下文类
	public final Class<P> PLAYER_CONTEXT_CLASS;

	// 全部在线角色
	public final Map<Long, P> PLAYER_CONTEXT_MAP;

	// 文件配置
	public final PC PLAYER_CONFIGURE;

	// 角色类
	public final Class<? extends JbPlayer> PLAYER_CLASS;

	// 角色附加属性类
	public final Class<? extends JbPlayerA> PLAYERA_CLASS;

	// 好友类
	public final Class<? extends JbFriend> FRIEND_CLASS;

	// 角色等级定义
	protected List<PD> playerDefines;

	// 卡牌定义
	protected XlsDao<CD, Serializable> cardDefineDao;

	// 卡牌等级定义
	protected List<IExp> cardExps;

	// 卡牌等级经验
	protected List<Integer> cardLevelExps = new ArrayList<Integer>();

	// 道具定义
	protected XlsDao<PP, Serializable> propDefineDao;

	// 奖励定义
	protected XlsDao<RD, Serializable> rewardDefineDao;

	// 奖励定义
	protected XlsDao<TD, Serializable> taskDefineDao;

	/**
	 * 初始化
	 */
	public PlayerComponentBase() {
		Class<?>[] componentClasses = KernelClass.componentClasses(getClass());
		PLAYER_CONTEXT_CLASS = (Class<P>) componentClasses[1];
		PLAYER_CONTEXT_MAP = (Map<Long, P>) (Object) ContextUtils.getContextFactory().getContextMap(PLAYER_CONTEXT_CLASS);
		PLAYER_CONFIGURE = (PC) JConfigureUtils.getConfigure((Class<? extends JConfigureBase>) componentClasses[2]);

		componentClasses = KernelClass.componentClasses(PLAYER_CONTEXT_CLASS);
		PLAYER_CLASS = (Class<? extends JbPlayer>) componentClasses[1];
		PLAYERA_CLASS = (Class<? extends JbPlayerA>) componentClasses[2];
		FRIEND_CLASS = (Class<? extends JbFriend>) componentClasses[5];
	}

	/**
	 * 初始化组件
	 */
	@Inject
	public void reloadComponent() {
		// 读取配置文件
		DynaBinder.INSTANCE.mapBind(ConfigureUtils.readPropertyMap(new File(BeanFactoryUtils.getBeanConfig().getClassPath() + "conf/PlayComponent.conf")), this);
		// 初始化配置对象
		Class<?>[] componentClasses = KernelClass.componentClasses(getClass());
		playerDefines = (List<PD>) XlsUtils.getXlsBeans((Class<? extends XlsBase>) componentClasses[3]);
		cardDefineDao = (XlsDao<CD, Serializable>) XlsUtils.getXlsDao((Class<? extends XlsBase>) componentClasses[4]);
		cardExps = (List<IExp>) XlsUtils.getXlsDao((Class<? extends XlsBase>) componentClasses[5]).getAll();
		int exp = 0;
		cardLevelExps.clear();
		for (IExp iExp : cardExps) {
			exp += iExp.getExp();
			cardLevelExps.add(exp);
		}

		propDefineDao = (XlsDao<PP, Serializable>) XlsUtils.getXlsDao((Class<? extends XlsBase>) componentClasses[6]);
		rewardDefineDao = (XlsDao<RD, Serializable>) XlsUtils.getXlsDao((Class<? extends XlsBase>) componentClasses[7]);
		taskDefineDao = (XlsDao<TD, Serializable>) XlsUtils.getXlsDao((Class<? extends XlsBase>) componentClasses[8]);
	}

	/**
	 * @return the cardDefineDao
	 */
	public XlsDao<CD, Serializable> getCardDefineDao() {
		return cardDefineDao;
	}

	/**
	 * 创建卡牌
	 * 
	 * @return
	 */
	public abstract C createCard();

	/**
	 * 创建角色
	 * 
	 * @return
	 */
	public abstract JbPlayer createPlayer();

	/**
	 * 创建角色附加
	 * 
	 * @return
	 */
	public abstract JbPlayerA<?> createPlayerA();

	/**
	 * 创建好友关系
	 * 
	 * @return
	 */
	public abstract JbFriend createFriend();

	/**
	 * 获取在线玩家
	 * 
	 * @param playerId
	 * @return
	 */
	public P find(Long playerId) {
		return PLAYER_CONTEXT_MAP.get(playerId);
	}

	/**
	 * 在线玩家数据
	 * 
	 * @param player
	 * @return
	 */
	public <T extends JbPlayer> T onlinePlayer(T player) {
		P playerContext = find(player.getId());
		return playerContext == null ? player : (T) playerContext.getPlayer();
	}

	/**
	 * 在线玩家数据
	 * 
	 * @param players
	 */
	public <T extends JbPlayer> List<T> onlinePlayers(List<T> players) {
		int size = players.size();
		T player;
		T onlinePlayer;
		for (int i = 0; i < size; i++) {
			player = players.get(i);
			onlinePlayer = onlinePlayer(player);
			if (player != onlinePlayer) {
				players.set(i, onlinePlayer);
			}
		}

		return players;
	}

	/**
	 * 在线玩家数据
	 * 
	 * @param Iterator
	 * @return
	 */
	public <T extends JbPlayer> List<T> onlinePlayers(Iterator<T> playerIterator) {
		List<T> players = new ArrayList<T>();
		while (playerIterator.hasNext()) {
			players.add(onlinePlayer(playerIterator.next()));
		}

		return players;
	}

	/**
	 * 最大等级
	 * 
	 * @param playerContext
	 * @return
	 */
	public int getPlayerMaxLevel(P playerContext) {
		return PLAYER_CONFIGURE.getMaxLevel();
	}

	/**
	 * 经验列表
	 * 
	 * @return
	 */
	public List<PD> getPlayerExps() {
		return playerDefines;
	}

	/**
	 * 重置卡牌
	 * 
	 * @param card
	 */
	public void resetCard(C card) {
		card.setExp(0);
		modifyCardLevel(card, 1);
	}

	/**
	 * 获取卡牌定义
	 * 
	 * @param cardId
	 * @return
	 */
	public CD getCardDefine(Serializable cardId) {
		return cardDefineDao.get(cardId);
	}

	/**
	 * 获取道具定义
	 * 
	 * @param propId
	 * @return
	 */
	public PP getPropDefine(Serializable propId) {
		return propDefineDao.get(propId);
	}

	/**
	 * 获取道具定义
	 * 
	 * @param rewardId
	 * @return
	 */
	public RD getRewardDefine(Serializable rewardId) {
		return rewardDefineDao.get(rewardId);
	}

	/**
	 * 获取任务定义
	 * 
	 * @param taskId
	 * @return
	 */
	public TD getTaskDefine(Serializable taskId) {
		return taskDefineDao.get(taskId);
	}

	/**
	 * 创建卡牌
	 * 
	 * @param cardDefine
	 * @param level
	 * @return
	 */
	public C generateCard(ICardDefine cardDefine) {
		C card = createCard();
		card.setCardDefine(cardDefine);
		resetCard(card);
		return card;
	}

	/**
	 * 获取卡牌最大等级
	 * 
	 * @param card
	 * @return
	 */
	public int getCardMaxLevel(C card) {
		return card.getCardDefine().getMaxLevel();
	}

	// 设置卡牌等级
	/** CARD_LEVEL_EXP_CXT */
	protected final LevelExpCxt<C> CARD_LEVEL_EXP_CXT = new LevelExpCxt<C>() {

		@Override
		public void setLevel(C card, int level) {
			modifyCardLevel(card, level);
		}
	};

	/**
	 * 更改卡牌经验
	 * 
	 * @param card
	 * @param exp
	 */
	public void modifyCardExp(C card, int exp) {
		synchronized (card.getPlayer() == null ? card : card.getPlayer()) {
			GameUtils.modifyExp(exp, card, CARD_LEVEL_EXP_CXT, cardExps, getCardMaxLevel(card));
		}
	}

	/**
	 * 更改卡牌等级
	 * 
	 * @param card
	 * @param level
	 */
	public void modifyCardLevel(C card, int level) {
		if (card.getLevel() == level || level < 1 || level > getCardMaxLevel(card)) {
			return;
		}

		synchronized (card.getPlayer() == null ? card : card.getPlayer()) {
			doModifyCardLevel(card, level);
		}
	}

	/**
	 * 更改卡牌等级
	 * 
	 * @param card
	 * @param level
	 */
	protected void doModifyCardLevel(C card, int level) {
		card.setLevel(level);
	}

	/**
	 * 获取升级经验
	 * 
	 * @param target
	 * @param card
	 * @return
	 */
	public int getFeedExp(C target, C card) {
		return (cardLevelExps.get(card.getLevel()) + card.getExp());
	}

	/**
	 * 获取升级金钱
	 * 
	 * @param target
	 * @param allExp
	 * @return
	 */
	public int getFeedMoney(C target, int allExp, List<C> cards) {
		return target.getCardDefine().getFeedPrice() + allExp * 10;
	}

	/**
	 * 获取卡牌进化几率
	 * 
	 * @param target
	 * @return
	 */
	public float getEvoluteOdds(C target) {
		return 1;
	}
}
