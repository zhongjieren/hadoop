package hdfs;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.BlockLocation;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.hdfs.protocol.DatanodeInfo;
import org.apache.hadoop.io.IOUtils;
/**
 * FileSystem 文件处理
 * @author arenzhj
 *
 */
public class App2 {
	static final String PATH = "hdfs://arenzhj:9000/";
	static final String DIR = "/d1";
	static final String FILE = "/d1/hello";
	static final String NEWFILE = "/d1/newhello";
	public static void main(String[] args) throws Exception {
		FileSystem fileSystem = getFileSystem();
		//创建文件夹     hadoop fs -mkdir   /f1
//		mkdir(fileSystem);
		//上传文件  -put  src  des
//		putData(fileSystem);
		renameFile(fileSystem, new Path(FILE), new Path(NEWFILE));
		//下载文件   hadoop fs -get src des
//		getData(fileSystem);
		//浏览文件夹
		list(fileSystem);
		//删除文件夹
		remove(fileSystem);
		list(fileSystem);
	}
	//浏览文件夹
	private static void list(FileSystem fileSystem) throws IOException {
		final FileStatus[] listStatus = fileSystem.listStatus(new Path("/"));
		System.out.println("文件类型\t权限\t\t副本数目\t文件大小\t文件路径");
		listFiles(listStatus,fileSystem);
	}
	
	private static void listFiles(FileStatus[] listStatus,FileSystem fileSystem) throws IOException{
		for (FileStatus fileStatus : listStatus) {
			String isDir = fileStatus.isDir()?"文件夹":"文件";
			if(fileStatus.isDir()){
				FileStatus[] listSubStatus = fileSystem.listStatus(fileStatus.getPath());
				listFiles(listSubStatus,fileSystem);
			} 
			final String permission = fileStatus.getPermission().toString();
			final short replication = fileStatus.getReplication();
			final long len = fileStatus.getLen();
			final String path = fileStatus.getPath().toString();
			System.out.println(isDir+"\t"+permission+"\t"+replication+"\t"+len+"\t"+path);
			System.out.println("-----------------查看该文件的精确位置--Start----------------");
			//查看该文件的精确位置
			BlockLocation[] blockLocations = fileSystem.getFileBlockLocations(fileStatus, 0, fileStatus.getLen());
			for(BlockLocation blockLocation:blockLocations){
				String[] hosts=blockLocation.getHosts();
				for(String host :hosts){
					System.out.print(host+"\t");
				}
			}
			System.out.println("-----------------查看该文件的精确位置--End----------------");
			
		}
	}
	/**
	 * 显示文件内容
	 * @param fileSystem
	 * @throws IOException
	 */
	private static void getData(FileSystem fileSystem) throws IOException {
		final FSDataInputStream in = fileSystem.open(new Path(FILE));
		IOUtils.copyBytes(in, System.out, 1024, true);
	}
	/**
	 * 上传文件
	 * @param fileSystem
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private static void putData(FileSystem fileSystem) throws IOException,
			FileNotFoundException {
		final FSDataOutputStream out = fileSystem.create(new Path(FILE));
		final FileInputStream in = new FileInputStream("C:/project/software/hadoop/note/02.txt");
		IOUtils.copyBytes(in, out, 1024, true);
	}
	/**
	 * 删除文件夹
	 * @param fileSystem
	 * @throws IOException
	 */
	private static void remove(FileSystem fileSystem) throws IOException {
		fileSystem.delete(new Path(DIR), true);
	}
	/**
	 * 创建文件夹 
	 * @param fileSystem
	 * @throws IOException
	 */
	private static void mkdir(FileSystem fileSystem) throws IOException {
		fileSystem.mkdirs(new Path(DIR));
	}
	/**
	 * 根据虚拟目录获取HDFS
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	private static FileSystem getFileSystem() throws IOException, URISyntaxException {
		return FileSystem.get(new URI(PATH), new Configuration());
	}
	
	/**
	 * 显示集群各节点信息
	 * @param fileSystem
	 * @throws IOException
	 */
	private static void showClusters(FileSystem fileSystem) throws IOException {
		DistributedFileSystem distributeFs=(DistributedFileSystem)fileSystem;
		DatanodeInfo[] datanodeInfos = distributeFs.getDataNodeStats();
		System.out.println("--------集群各节点信息如下:");
		for(DatanodeInfo datanodeInfo :datanodeInfos){
			String hostName = datanodeInfo.getHost();
			System.out.println(hostName);
		}
	}
	/**
	 * 重名名文件
	 * @param fileSystem
	 * @throws IOException
	 */
	private static void renameFile(FileSystem fileSystem,Path srcPath,Path desPath) throws IOException {
		System.out.println("----"+desPath+"重名名为:"+desPath);
		boolean flag = fileSystem.rename(srcPath,desPath);
		System.out.println(flag?"命名成功!":"命名失败!");
	}
}
