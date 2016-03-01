/**
 * 
 */
package com.geekie.Location;

import com.amap.api.location.AMapLocation;

/**
 * 辅助工具类
 * @创建时间： 2015年11月24日 上午11:46:50
 * @项目名称： AMapLocationDemo2.x
 * @author hongming.wang
 * @文件名称: Utils.java
 * @类型名称: Utils
 */
public class Utils {
	/**
	 *  开始定位
	 */
	public final static int MSG_LOCATION_START = 0;
	/**
	 * 定位完成
	 */
	public final static int MSG_LOCATION_FINISH = 1;
	/**
	 * 停止定位
	 */
	public final static int MSG_LOCATION_STOP= 2;
	
	/**
	 * 根据定位结果返回定位信息的字符串
	 * @param location
	 * @return
	 */
	public synchronized static String getLocationStr(AMapLocation location){
		if(null == location){
			return null;
		}
		StringBuffer sb = new StringBuffer();
		//errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
		if(location.getErrorCode() == 0){
			sb.append("===定位成功===" + "\n");
			sb.append("类型: " + location.getLocationType() + "\n");
			sb.append("经度: " + location.getLongitude() + "\n");
			sb.append("纬度: " + location.getLatitude() + "\n");
			sb.append("精度: " + location.getAccuracy() + "m" + "\n");
			sb.append("来源: " + location.getProvider() + "\n");
			
			if (location.getProvider().equalsIgnoreCase(
					android.location.LocationManager.GPS_PROVIDER)) {
				// 以下信息只有提供者是GPS时才会有
				sb.append("速度: " + location.getSpeed() + "m/s" + "\n");
				sb.append("角度: " + location.getBearing() + "\n");
				// 获取当前提供定位服务的卫星个数
				sb.append("星数: "
						+ location.getSatellites() + "\n");
			} else {
				// 提供者是GPS时是没有以下信息的
				//sb.append("国家: " + location.getCountry() + "\n");
				//sb.append("省 : " + location.getProvince() + "\t\t\t");
				//sb.append("市 : " + location.getCity() + "\t\t\t");
				//sb.append("区 : " + location.getDistrict() + "\n");
				//sb.append("市编码: " + location.getCityCode() + "\t\t\t");
				//sb.append("区域码: " + location.getAdCode() + "\n");
				sb.append("地址: " + location.getAddress() + "\t\t");
				sb.append("( " + location.getCityCode() );
				sb.append(" | " + location.getAdCode() + " )\n");
				sb.append("周边: " + location.getPoiName() + "\n");
			}
		} else {
			//定位失败
			sb.append("***定位失败****" + "\n");
			sb.append("错误码:" + location.getErrorCode() + "\n");
			sb.append("错误信息:" + location.getErrorInfo() + "\n");
			sb.append("错误描述:" + location.getLocationDetail() + "\n");
		}
		return sb.toString();
	}
}
