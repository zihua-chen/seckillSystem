package org.seckill.service.impl;

import org.seckill.dao.SeckillDao;
import org.seckill.dao.SuccessKillDao;
import org.seckill.dao.cache.RedisDao;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SeckillStateEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service(value="seckillService")
public class SeckillServiceImpl implements SeckillService {

    //日志对象API

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    //注入redisDao
    @Autowired
    private RedisDao redisDao;

    //秒杀dao
    //注入servi依赖
    @Autowired()
    private SeckillDao seckillDao;

    //秒杀成功dao
    @Autowired
    private SuccessKillDao successKilledDao;

    //设定一个盐值字符串，用户混淆MD5
    private final String slat = "fjdajgqjofneodnoaj265165fdkjoang/'da^&*";
    @Override
    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0,4);
    }

    @Override
    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    @Override
    public Exposer exportSeckillUrl(long seckillId) {
        //优化点：缓存优化,超时的基础上维护一致性
        //1.访问redis
        Seckill seckill = redisDao.getSeckill(seckillId);
        if(seckill == null){
            //2.访问数据库
            seckill=seckillDao.queryById(seckillId);
            if(seckill == null){
                return new Exposer(false,seckillId);
            }else{
                //3.放入redis
                redisDao.putSeckill(seckill);
            }
        }
        Date startTime = seckill.getStartTime();
        Date endTime=seckill.getEndTime();
        //系统当前时间
        Date nowTime = new Date();
        if(nowTime.getTime() > endTime.getTime() || nowTime.getTime() < startTime.getTime()){
            return new Exposer(false,seckillId,nowTime.getTime(),startTime.getTime());
        }
        //返回true，并用md5做加密
        //md5的本身就是对任意字符串可以转换一个编码，它是不可逆的
        //转换字符串的过程
        String md5 = getMD5(seckillId);
        return new Exposer(true,md5,seckillId);

    }

    @Override
    @Transactional
    /**
     * 使用注解控制事务方法的优点：
     * 1：开发团队达成一致约定，约定明确标注事务方法的编程风格
     * 2：保证事务方法的执行时间尽可能短，不要穿插的网络操作，比如，RPC/HTTP请求或者剥离到事务方法外部
     * 3：不是所有的方法都需要事务，如：只有一条数据库修改操作，或者只读操作
     */
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5)
            throws SeckillException {
        if(md5 == null || !md5.equals(getMD5(seckillId))){
            throw new SeckillException("seckill data rewrite");
        }
        Date nowTime = new Date();
        try {
            //记录购买行为
            int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
            //唯一：seckillId，userPhone
            if (insertCount <= 0) {
                //重复秒杀
                throw new RepeatKillException("seckill repeated");
            } else {
                // 执行秒杀逻辑：减库存+记录购买行为
                //热点商品竞争
                int updateCount = seckillDao.reduceNumber(seckillId, nowTime);
                if (updateCount <= 0) {
                    //没有更新到记录
                    throw new SeckillCloseException("seckill is closed");
                } else {
                    //秒杀成功
                    SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                    //在这里做了一个枚举
                    return new SeckillExecution(seckillId, SeckillStateEnum.SUCCESS, successKilled);
                }
            }
        }catch (SeckillCloseException e1){
            throw e1;
        }
        catch (RepeatKillException e2){
            throw e2;
        }
        catch (Exception e){
            logger.error(e.getMessage(),e);
            //所有编译器异常转化为允许期异常
            throw new SeckillException("seckill inner error:"+e.getMessage());
        }
    }

    /**
     * 获取MD5值得方法，里面包含了拼写规则
     * @param seckillId
     * @return
     */
    private String getMD5(long seckillId){
        String base = seckillId + "/" +slat;
        //使用Spring的一个工具类，生成了MD5
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }
}
