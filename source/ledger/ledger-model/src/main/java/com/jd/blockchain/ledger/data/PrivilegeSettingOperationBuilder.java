package com.jd.blockchain.ledger.data;

import com.jd.blockchain.ledger.PrivilegeType;

/**
 * 账户权限设置操作；
 * 
 * <br>
 * 
 * 注：默认情况下，在账户被注册时，账户自身会包含在权限设置表中，具有全部的权限； <br>
 * 
 * 但这不是必须的，使用者可以根据业务需要，去掉账户自身的权限，并将权限赋予其它的账户，以此实现将区块链账户分别用于表示“角色”和“数据”这两种目的；
 * 
 * @author huanghaiquan
 *
 */
public interface PrivilegeSettingOperationBuilder {
	
	PrivilegeSettingOperationBuilder setThreshhold(PrivilegeType privilege, long threshhold);

	PrivilegeSettingOperationBuilder enable(PrivilegeType privilege, String address, int weight);

	PrivilegeSettingOperationBuilder disable(PrivilegeType privilege, String address);

}
