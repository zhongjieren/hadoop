package rpc;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.RPC;
import org.apache.hadoop.ipc.RPC.Server;

public class MyServer {
	static final String ADDRESS = "localhost";
	static final int PORT = 12345;
	public static void main(String[] args)throws Exception {
		/** 
		 * 构造一个RPC的服务端.
	     * @param instance 这个实例中的方法会被调用
	     * @param bindAddress 绑定的地址是用于监听连接的
	     * @param port 绑定的端口是用于监听连接的
	     * @param conf the configuration to use
	     */
		final Server server = RPC.getServer(new MyBiz(), ADDRESS, PORT, new Configuration());
		server.start();
	}

}
