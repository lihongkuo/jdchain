package com.jd.blockchain.ledger;

import com.jd.blockchain.ContractDeployMojo;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * for contract deploy and exe;
 * @Author zhaogw
 * @Date 2018/11/02 09:06
 */
public class ContractDeployMojoTest {
    private ContractDeployMojo contractDeployMojo = new ContractDeployMojo();

    private void fieldHandle(String fieldName,Object objValue) throws NoSuchFieldException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        Field field = contractDeployMojo.getClass().getDeclaredField(fieldName);//name为类Instance中的private属性
        field.setAccessible(true);//=true,可访问私有变量。
        Class<?> typeClass = field.getType();
        field.set(contractDeployMojo, objValue);
    }
}
