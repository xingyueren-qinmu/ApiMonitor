package com.softsec.mobsec.dae.apimonitor.hook.hookUtils;



import android.util.Base64;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CLogUtils {

	//规定每段显示的长度
	private static int LOG_MAXLENGTH = 2000;

	private static String TAG = "XposedInfo";
	private static String NETTAGORI = "XposedNet";
	private static String NetTAG = "OkHttpHookInterceptor";

	private static final Logger logger = new Logger();

	public static void e(String msg){
		InfiniteLog(TAG, msg);
	}

	public static void NetLoggerOri(String msg){
		InfiniteLog(NETTAGORI, msg);
	}

	public static void NetLogger(String fullmsg) {

		logger.setTag(NetTAG);
		String msg = fullmsg.split("StackElements:\n")[0];
		String callingInfo = fullmsg.split("StackElements:\n")[1];
		String callingInfo1 = callingInfo.split("\nxrefFrom:\n")[0];
		String callingInfo2 = callingInfo.split("\nxrefFrom:\n")[1];

		Map<String, String> analyzeResult = analyze(msg);
		if(msg.startsWith("Request:\n") || msg.startsWith("请求Url:\n")){
			String request_raw = Base64.encodeToString(msg.getBytes(), Base64.NO_WRAP|Base64.NO_PADDING|Base64.URL_SAFE);
			logger.setCallingInfo(callingInfo1);
			logger.addRelatedAttrs("request_raw",request_raw);
			logger.addRelatedAttrs("xrefFrom", callingInfo2);
			logger.recordAPICallingAdd("okhttp请求",
					"method",analyzeResult.get("method"),
					"url",analyzeResult.get("url"),
					"host",analyzeResult.get("Host"),
					"request_size",analyzeResult.get("request_size"),
					"protocol",analyzeResult.get("Protocol"));
		}else if(msg.startsWith("Response:\n") || msg.startsWith("响应Url:\n")){
			String response_raw = Base64.encodeToString(msg.getBytes(), Base64.NO_WRAP|Base64.NO_PADDING|Base64.URL_SAFE);
			logger.setCallingInfo(callingInfo1);
			logger.addRelatedAttrs("response_raw",response_raw);
			logger.addRelatedAttrs("xrefFrom", callingInfo2);
			logger.recordAPICallingAdd("okhttp响应",
					"code",analyzeResult.get("code"),
					"url",analyzeResult.get("url"),
					"response_size",analyzeResult.get("response_size"));
		}

	}

	public static Map<String,String> analyze(String msg){
		Pattern pattern = Pattern.compile("\\d+");

		Map<String, String> result = new HashMap<>();

		if(msg.startsWith("Request:\n")){
			String[] requestLines = msg.split("\n");
			for(String line : requestLines){
				if(line.startsWith("-->") && !line.startsWith("--> END")){
					String[] strings = line.split("\\s");
					result.put("method",strings[1]);
					result.put("url",strings[2]);
					if(strings[2].contains("://")){
						result.put("Host",strings[2].split("://")[1].split("/")[0]);
						result.put("Protocol",strings[2].split("://")[0]);
					}
				}else if(line.startsWith("--> END")){
					if(line.contains("-byte")){
						Matcher matcher = pattern.matcher(line);
						if(matcher.find()){
							result.put("request_size", matcher.group());
						}
					}else{
						result.put("request_size", "unknown");
					}
				}
			}
		}else if(msg.startsWith("Response:\n")){
			String[] lines = msg.split("\n");
			for(String line : lines){
				if(line.startsWith("<--") && !line.startsWith("<-- END")){
					String[] strings = line.split("\\s");
					result.put("code",strings[1]);
					result.put("url",strings[2]);
				}else if(line.startsWith("<-- END")){
					if(line.contains("-byte")){
						Matcher matcher = pattern.matcher(line);
						if(matcher.find()){
							result.put("response_size", matcher.group());
						}
					}else{
						result.put("response_size", "unknown");
					}
				}
			}
		}else if(msg.startsWith("请求Url:\n")){
			String urlinfo = msg.split("请求Url:\n")[1].split("请求头部信息:\n")[0];
			String headerinfo = msg.split("请求头部信息:\n")[1].split("请求body信息:\n")[0];
			String bodyinfo = msg.split("请求头部信息:\n")[1].split("请求body信息:\n")[1];

			String[] part1 = urlinfo.split("\\s");
			result.put("method",part1[1]);
			result.put("url",part1[2]);
			if(part1.length == 4){
				result.put("Protocol",part1[3]);
			}else{
				if(part1[2].contains("://")){
					result.put("Protocol",part1[2].split("://")[0]);
					result.put("Host",part1[2].split("://")[1].split("/")[0]);
				}
			}

			String[] part2 = headerinfo.split("\n");
			for(String line : part2){
				if(line.contains("Content-Length")){
					Matcher matcher = pattern.matcher(line);
					if(matcher.find()){
						result.put("request_size", matcher.group());
					}
				}
			}

			if(!result.containsKey("request_size")){
				String[] part3 = bodyinfo.split("\n");
				for(String line : part3){
					if(line.startsWith("--> END ")){
						Matcher matcher = pattern.matcher(line);
						if(matcher.find()){
							CLogUtils.e("special " + matcher.group());
							result.put("request_size", matcher.group());
						}else {
							result.put("request_size", "unknown");
						}
					}
				}
			}

		}else if(msg.startsWith("响应Url:\n")){
			String urlinfo = msg.split("响应Url:\n")[1].split("响应头部信息:\n")[0];

			String[] s1 = urlinfo.split("\\(");
			String[] s2 = s1[0].split("\\s");
			result.put("code",s2[1]);
			result.put("url",s2[s2.length-2]);
			if(s1[1].contains("byte")){
				result.put("bodysize",s1[1].split("ms")[1].split("-byte")[0].replace(" ",""));
			}else{
				result.put("bodysize","unknown");
			}

		}

		return result;
	}


	public static void e(String TAG, String msg){
		InfiniteLog(TAG, msg);
	}

	/**
	 * log最多 4*1024 长度 这个 方法 可以解决 这个问题
	 * @param TAG
	 * @param msg
	 */
	private static void InfiniteLog(String TAG, String msg) {
		int strLength = msg.length();
		int start = 0;
		int end = LOG_MAXLENGTH;
		for (int i = 0; i < 10000; i++) {
			//剩下的文本还是大于规定长度则继续重复截取并输出
			if (strLength > end) {
				Log.e(TAG + i, msg.substring(start, end));
				start = end;
				end = end + LOG_MAXLENGTH;
			} else {
				Log.e(TAG, msg.substring(start, strLength));
				break;
			}
		}
	}


}
