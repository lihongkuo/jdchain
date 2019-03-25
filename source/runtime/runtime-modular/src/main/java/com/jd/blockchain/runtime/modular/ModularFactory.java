package com.jd.blockchain.runtime.modular;

public class ModularFactory {

	/**
	 * start system；
	 */
	public static void startSystem(String runtimeDir, boolean productMode,
			ClassLoader libClassLoader,String mainClassName,  ClassLoader systemClassLoader, String[] args) {

		JarsModule libModule = new JarsModule("LibModule", libClassLoader);

		ModularRuntimeContext runtimeContext = new ModularRuntimeContext(runtimeDir, libModule, productMode);
		runtimeContext.register();
		
		SystemModule systemModule = new SystemModule(mainClassName, systemClassLoader, runtimeContext);
		systemModule.start(args);
	}

}
