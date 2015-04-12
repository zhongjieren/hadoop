package rpc;

import java.net.InetSocketAddress;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.RPC;

public class MyClient {

	public static void main(String[] args) throws Exception{
		/** 
		 * 构造一个客户端代理对象，该代理对象实现了命名的协议。代理对象会与指定地址的服务端通话
		 */
		MyBizable proxy = (MyBizable)RPC.waitForProxy(
					MyBizable.class,
					MyBizable.VERSION,
					new InetSocketAddress(MyServer.ADDRESS, MyServer.PORT),
					new Configuration());
		final String result = proxy.hello("world");
		System.out.println("客户端结果："+result);
		//关闭网络连接
		RPC.stopProxy(proxy);
	}

}
