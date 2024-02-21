package utils;

import net.daicy.trillion.common.encryp.MD5Util;
import net.daicy.trillion.common.util.CommonUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.StringJoiner;

/**
 * @author rgao
 * @description
 * @date 2024-02-03 11:31
 */
public class PathUtils {

	/**
	 * 计算文件夹位置
	 * @param dirName 文件夹名称
	 * @return 相对路径
	 */
	public static String generatorPath(Long dirName) {
		int p = 1000;
		if (dirName == null) {
			return null;
		}
		long l1 = dirName % p;
		long l2 = dirName / p;
		StringJoiner path = new StringJoiner("/");
		if (l2 > 1) {
			for (long i = 1; i < l2; i++) {
				path.add(String.valueOf(p * i + l1));
			}
		}
		return path + File.separator + dirName;
	}

	public static String generateNameMd5(String name) {
		if (CommonUtil.isNullOrEmpty(name)) {
			return "";
		}
		return MD5Util.MD5(name);
	}

	public static String readAllFiles(String path) throws IOException {
		byte[] contentBytes = Files.readAllBytes(Paths.get(path));
		return new String(contentBytes, StandardCharsets.UTF_8);
	}
}
