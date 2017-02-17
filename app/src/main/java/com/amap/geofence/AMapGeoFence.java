package com.amap.geofence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.amap.api.fence.GeoFence;
import com.amap.api.fence.GeoFenceClient;
import com.amap.api.fence.GeoFenceListener;
import com.amap.api.location.DPoint;
import com.amap.api.maps.AMap;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Polygon;
import com.amap.api.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by ligen on 16/12/15.
 */
public class AMapGeoFence implements GeoFenceListener {
    private String TAG = "AMapGeoFence";
    private GeoFenceClient mClientInAndStayAction;
    private GeoFenceClient mClientAllAction;
    private Context mContext;
    private AMap mAMap;
    private ConcurrentMap mCustomEntitys;
    private Handler mHandler;
    private int mCustomID = 100;
    // 记录已经添加成功的围栏
    private volatile ConcurrentMap<String, GeoFence> fenceMap = new ConcurrentHashMap<String, GeoFence>();
    // 地理围栏的广播action
    static final String GEOFENCE_BROADCAST_ACTION = "com.amap.geofence";

    private ExecutorService mThreadPool;

    //朝阳公园
    private static final String mPolygonFenceString1 = "116.48954,39.949035;116.48956,39.946986;116.489545,39.945793;116.489545,39.945151;116.489547,39.944534;116.489537,39.942016;116.489542,39.940404;116.489532,39.939772;116.489516,39.936447;116.489476,39.935253;116.489468,39.933894;116.488858,39.933901;116.488646,39.933904;116.486819,39.933926;116.486029,39.933919;116.482808,39.933924;116.481209,39.933928;116.477237,39.93393;116.474738,39.93395;116.474632,39.934851;116.474466,39.935205;116.474103,39.935351;116.473723,39.935406;116.473488,39.935378;116.473412,39.935423;116.4734,39.93551;116.473558,39.935659;116.473987,39.936095;116.474344,39.936446;116.474609,39.936707;116.474815,39.936986;116.474899,39.937155;116.474915,39.93733;116.474916,39.937721;116.474919,39.93805;116.4749,39.938376;116.474871,39.938953;116.474855,39.939258;116.474816,39.93977;116.474781,39.940558;116.474724,39.941285;116.474689,39.941889;116.474649,39.942447;116.474628,39.942775;116.474608,39.943028;116.474601,39.943117;116.474551,39.943673;116.474499,39.944207;116.474488,39.944354;116.474467,39.944524;116.474415,39.945015;116.47436,39.945305;116.474335,39.945401;116.474247,39.945691;116.47414,39.946063;116.47393,39.946832;116.473854,39.94711;116.473716,39.94752;116.47362,39.947742;116.473991,39.947865;116.474236,39.947932;116.474385,39.947925;116.474506,39.94798;116.474887,39.948182;116.475212,39.948319;116.475795,39.948419;116.476327,39.948544;116.476614,39.948657;116.477115,39.948675;116.477472,39.948712;116.477793,39.948754;116.478074,39.948851;116.478588,39.948964;116.478705,39.949009;116.478752,39.949069;116.478818,39.949282;116.47883,39.949374;116.478824,39.949446;116.478789,39.949503;116.478648,39.949599;116.478616,39.949629;116.478538,39.949775;116.478451,39.949898;116.478378,39.94997;116.478336,39.950033;116.478318,39.950072;116.478328,39.950211;116.478418,39.950352;116.478481,39.95041;116.47855,39.950433;116.478575,39.95046;116.478593,39.950512;116.478417,39.950964;116.478236,39.951219;116.478168,39.951292;116.477863,39.951713;116.476722,39.953685;116.476775,39.953867;116.479074,39.954626;116.480838,39.955196;116.483278,39.955976;116.483806,39.956138;116.484569,39.956407;116.485044,39.956637;116.486039,39.95704;116.486266,39.957121;116.487219,39.957523;116.487311,39.95754;116.487474,39.957491;116.48762,39.957241;116.487655,39.957173;116.487715,39.957055;116.487805,39.956968;116.488078,39.956501;116.488223,39.956233;116.488432,39.955773;116.488625,39.955281;116.4887,39.95508;116.488843,39.954612;116.489033,39.953949;116.489154,39.95345;116.489218,39.953167;116.489278,39.952847;116.489303,39.952651;116.489309,39.952553;116.489299,39.952429;116.489257,39.952256;116.48923,39.952153;116.489223,39.951986;116.489266,39.951738;116.489317,39.951608;116.48936,39.95151;116.489402,39.951426;116.489434,39.951363;116.489466,39.951212;116.489493,39.950603;116.48954,39.94958;116.48954,39.949035";
//    //国家大剧院
//    private static final String mPolygonFenceString2 = "116.389616,39.907136;116.390681,39.907168;116.391089,39.90717;116.391232,39.90715;116.391389,39.90705;116.391487,39.906872;116.391501,39.906679;116.39152,39.906315;116.391543,39.905835;116.391567,39.905389;116.391613,39.904339;116.391639,39.903778;116.391648,39.903532;116.39163,39.903449;116.391601,39.903244;116.391568,39.9031;116.391549,39.903001;116.39154,39.902951;116.391505,39.902903;116.387997,39.90279;116.387966,39.903003;116.387913,39.903319;116.387921,39.903662;116.387969,39.904064;116.388062,39.904783;116.388079,39.904901;116.388101,39.905084;116.388111,39.905174;116.388141,39.905407;116.38818,39.905694;116.388214,39.905926;116.388226,39.90599;116.388257,39.906222;116.3883,39.906592;116.388299,39.906697;116.38832,39.907107;116.389616,39.907136";
//    //望京华鼎世家小区
//    private static final String mPolygonFenceString3 = "116.474505,39.993538;116.473972,39.99388;116.472866,39.994536;116.472874,39.994766;116.474944,39.996576;116.475095,39.996609;116.47591,39.996682;116.476039,39.996664;116.47718,39.995907;116.477229,39.995747;116.47642,39.994899;116.474854,39.993558;116.474505,39.993538";
//    //望京伯爵城中央公园
//    private static final String mPolygonFenceString4 = "116.480275,39.993941;116.477538,39.991476;116.474882,39.993172;116.475815,39.994027;116.477569,39.995581;116.477754,39.995577;116.477913,39.995519;116.480275,39.993941";
//    //凯德mall望京
//    private static final String mPolygonFenceString5 = "116.468262,39.994122;116.468526,39.993703;116.46873,39.993502;116.469134,39.993203;116.469809,39.992724;116.469842,39.992655;116.469849,39.992601;116.469834,39.992549;116.469781,39.992482;116.469452,39.992109;116.469053,39.991715;116.468598,39.992054;116.468564,39.992084;116.468535,39.992114;116.468523,39.99215;116.468512,39.992249;116.468454,39.992702;116.468403,39.992809;116.468317,39.992904;116.467836,39.993263;116.467757,39.993327;116.467702,39.993402;116.467651,39.99351;116.467641,39.993618;116.467647,39.993816;116.467651,39.993957;116.467661,39.994029;116.467695,39.994089;116.467741,39.994118;116.467811,39.994141;116.467886,39.99416;116.467979,39.994171;116.468087,39.994177;116.468152,39.994165;116.4682,39.994155;116.468262,39.994122";

    //地铁望京站
    private static final DPoint mDpoint1 = new DPoint(39.998919, 116.467841);
    //中央美院
    private static final DPoint mDpoint2 = new DPoint(39.983631, 116.463721);
    //新世界百货
    private static final DPoint mDpoint3 = new DPoint(39.98659, 116.480629);
    //798艺术区
    private static final DPoint mDpoint4 = new DPoint(39.985202, 116.495681);
    //望京医院
    private static final DPoint mDpoint5 = new DPoint(39.982473, 116.473108);
    //绿地中心写字楼
    private static final DPoint mDpoint6 = new DPoint(39.999767, 116.488043);
    //鸟巢
    private static final DPoint mDpoint7 = new DPoint(39.993471, 116.396048);
    //央视大裤衩
    private static final DPoint mDpoint8 = new DPoint(39.915094, 116.463683);
    //环球金融中心写字楼
    private static final DPoint mDpoint9 = new DPoint(39.918912, 116.459391);
    //三里屯
    private static final DPoint mDpoint10 = new DPoint(39.938526, 116.45437);


    public AMapGeoFence(Context context, AMap amap, Handler handler) {
        mContext = context;
        mHandler = handler;
        mThreadPool = Executors.newCachedThreadPool();
        mCustomEntitys = new ConcurrentHashMap<String, Object>();
        mAMap = amap;
        IntentFilter fliter = new IntentFilter(
                ConnectivityManager.CONNECTIVITY_ACTION);
        fliter.addAction(GEOFENCE_BROADCAST_ACTION);
        mContext.registerReceiver(mGeoFenceReceiver, fliter);
        addFenceInAndStay();
        addFenceAll();
    }

    private void addFenceInAndStay() {
        mClientInAndStayAction = new GeoFenceClient(mContext);
        mClientInAndStayAction.createPendingIntent(GEOFENCE_BROADCAST_ACTION);
        mClientInAndStayAction.setGeoFenceListener(this);
        mClientInAndStayAction.setActivateAction(GeoFenceClient.GEOFENCE_IN | GeoFenceClient.GEOFENCE_STAYED);

        mClientInAndStayAction.addGeoFence("麦当劳", "快餐厅", "北京", 2, String.valueOf(mCustomID));
        mCustomID++;
        mClientInAndStayAction.addGeoFence("kfc", "快餐厅", new DPoint(39.982375,116.305292), 5000, 2, String.valueOf(mCustomID));
        mCustomID++;
        mClientInAndStayAction.addGeoFence("西城区", String.valueOf(mCustomID));
        mCustomID++;
    }

    private void addFenceAll() {
        mClientAllAction = new GeoFenceClient(mContext);
        mClientAllAction.createPendingIntent(GEOFENCE_BROADCAST_ACTION);
        mClientAllAction.setGeoFenceListener(this);
        mClientAllAction.setActivateAction(GeoFenceClient.GEOFENCE_IN | GeoFenceClient.GEOFENCE_STAYED | GeoFenceClient.GEOFENCE_OUT);

        addPolygonGeoFence(mPolygonFenceString1);
//        addPolygonGeoFence(mPolygonFenceString2);
//        addPolygonGeoFence(mPolygonFenceString3);
//        addPolygonGeoFence(mPolygonFenceString4);
//        addPolygonGeoFence(mPolygonFenceString5);

        addCircleGeoFence(mDpoint1);
        addCircleGeoFence(mDpoint2);
        addCircleGeoFence(mDpoint3);
        addCircleGeoFence(mDpoint4);
        addCircleGeoFence(mDpoint5);
        addCircleGeoFence(mDpoint6);
        addCircleGeoFence(mDpoint7);
        addCircleGeoFence(mDpoint8);
        addCircleGeoFence(mDpoint9);
        addCircleGeoFence(mDpoint10);
    }

    private void addPolygonGeoFence(String points) {
        mClientAllAction.addGeoFence(Util.toAMapGeoFenceList(points), String.valueOf(mCustomID));
        mCustomID++;
    }

    private void addCircleGeoFence(DPoint dPoint) {
        mClientAllAction.addGeoFence(dPoint, 1000, String.valueOf(mCustomID));
        mCustomID++;
    }

    private void drawPolygon(GeoFence fence) {
        final List<List<DPoint>> pointList = fence.getPointList();
        if (null == pointList || pointList.isEmpty()) {
            return;
        }
        List<Polygon> polygonList = new ArrayList<Polygon>();
        for (List<DPoint> subList : pointList) {
            if (subList == null) {
                continue;
            }
            List<LatLng> lst = new ArrayList<LatLng>();

            PolygonOptions polygonOption = new PolygonOptions();
            for (DPoint point : subList) {
                lst.add(new LatLng(point.getLatitude(), point.getLongitude()));
//                boundsBuilder.include(
//                        new LatLng(point.getLatitude(), point.getLongitude()));
            }
            polygonOption.addAll(lst);

            polygonOption.fillColor(mContext.getResources().getColor(R.color.fill));
            polygonOption.strokeColor(mContext.getResources().getColor(R.color.stroke));
            polygonOption.strokeWidth(4);
            Polygon polygon = mAMap.addPolygon(polygonOption);
            polygonList.add(polygon);
            mCustomEntitys.put(fence.getFenceId(), polygonList);
        }
    }

    private void drawCircle(GeoFence fence) {
        CircleOptions option = new CircleOptions();
        option.fillColor(mContext.getResources().getColor(R.color.fill));
        option.strokeColor(mContext.getResources().getColor(R.color.stroke));
        option.strokeWidth(4);
        option.radius(fence.getRadius());
        DPoint dPoint = fence.getCenter();
        option.center(new LatLng(dPoint.getLatitude(), dPoint.getLongitude()));
        Circle circle = mAMap.addCircle(option);
        mCustomEntitys.put(fence.getFenceId(), circle);
    }

    public void drawFenceToMap() {
        Iterator iter = fenceMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            GeoFence val = (GeoFence) entry.getValue();
            if (!mCustomEntitys.containsKey(key)) {
                Log.d("LG", "添加围栏:" + key);
                drawFence(val);
            }
        }
    }

    private void drawFence(GeoFence fence) {
        switch (fence.getType()) {
            case GeoFence.TYPE_ROUND:
            case GeoFence.TYPE_AMAPPOI:
                drawCircle(fence);
                break;
            case GeoFence.TYPE_POLYGON:
            case GeoFence.TYPE_DISTRICT:
                drawPolygon(fence);
                break;
            default:
                break;
        }

        // 设置所有maker显示在当前可视区域地图中
//        LatLngBounds bounds = boundsBuilder.build();
//        mAMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));
//        polygonPoints.clear();
//        removeMarkers();
    }

    public void removeAll() {
        try {
            mClientInAndStayAction.removeGeoFence();
            mClientAllAction.removeGeoFence();
            mContext.unregisterReceiver(mGeoFenceReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Object lock = new Object();

    @Override
    public void onGeoFenceCreateFinished(List<GeoFence> geoFenceList, int errorCode, String s) {
        if (errorCode == GeoFence.ADDGEOFENCE_SUCCESS) {
            for (GeoFence fence : geoFenceList) {
                Log.d("LG", "fenid:" + fence.getFenceId() + " customID:" + s + " " + fenceMap.containsKey(fence.getFenceId()));
                fenceMap.putIfAbsent(fence.getFenceId(), fence);
            }
            Log.d("LG", "回调添加成功个数:" + geoFenceList.size());
            Log.d("LG", "回调添加围栏个数:" + fenceMap.size());
            Message message = mHandler.obtainMessage();
            message.obj = geoFenceList;
            message.what = 0;
            mHandler.sendMessage(message);
            Log.e(TAG, "添加围栏成功！！");
        } else {
            Log.e(TAG, "添加围栏失败！！！！ errorCode: " + errorCode);
            Message msg = Message.obtain();
            msg.arg1 = errorCode;
            msg.what = 1;
            mHandler.sendMessage(msg);
        }
    }


    private BroadcastReceiver mGeoFenceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 接收广播
            if (intent.getAction().equals(GEOFENCE_BROADCAST_ACTION)) {
                Bundle bundle = intent.getExtras();
                String fenceID = bundle
                        .getString(GeoFence.BUNDLE_KEY_FENCEID);
                int status = bundle.getInt(GeoFence.BUNDLE_KEY_FENCESTATUS);
                StringBuffer sb = new StringBuffer();
                switch (status) {
                    case GeoFence.STATUS_LOCFAIL:
                        sb.append("定位失败");
                        break;
                    case GeoFence.STATUS_IN:
                        sb.append("进入围栏 ").append(fenceID);
                        break;
                    case GeoFence.STATUS_OUT:
                        sb.append("离开围栏 ").append(fenceID);
                        break;
                    case GeoFence.STATUS_STAYED:
                        sb.append("停留在围栏内 ").append(fenceID);
                        break;
                    default:
                        break;
                }
                String str = sb.toString();
                Message msg = Message.obtain();
                msg.obj = str;
                msg.what = 2;
                mHandler.sendMessage(msg);
            }
        }
    };
}
