import com.google.gson.Gson;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.UserAuthResponse;
import com.vk.api.sdk.objects.friends.responses.GetResponse;
import java.util.*;
import java.util.concurrent.TimeUnit;


//import static com.sun.org.apache.xalan.internal.xsltc.compiler.Constants.REDIRECT_URI;

/**
 * Created by Mikhail on 20.05.2017.
 */


public class Main {

    static Integer APP_ID = 0;
    static String CLIENT_SECRET = "";
    static String code = "";
    static String REDIRECT_URI = "https://oauth.vk.com/blank.html";
    String getSecret = "https://oauth.vk.com/authorize?client_id=6040264&redirect_uri=https://oauth.vk.com/blank.html";
    static String auth_uri = "https://oauth.vk.com/authorize?client_id="+APP_ID+"&redirect_uri="+REDIRECT_URI+"&response_type=code";

    static boolean haveFriends = false;

    static TransportClient transportClient = HttpTransportClient.getInstance();
    static VkApiClient vk = new VkApiClient(transportClient, new Gson());

    // вернет список (Map<Integer,Integer>) друзей в виде ребер, friend_user_id -> user_id
    public static Map<Integer,Integer> getFriendList(Integer user_id, UserActor actorUser){
        Map<Integer,Integer> friendsList = new HashMap<>();
        List<Integer> bufFriendsList = new ArrayList<>();
        try {
            GetResponse friendsArr = vk.friends().get(actorUser).userId(user_id).execute();
            bufFriendsList = friendsArr.getItems();
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }
        for(Integer item : bufFriendsList){
            friendsList.put(item,user_id);
        }
        return friendsList;
    }

    // вернет список (Map<Integer,Integer>) друзей друзей в виде ребер, friend_user_id -> user_id, и проверит на общих
    public static Map<Integer,Integer> getMutualFriends(Map<Integer,Integer>list_A/*для каких получить*/
            , Map<Integer,Integer>list_B/*с кем сравнить*/){
        Map<Integer,Integer> list_Mutual_Friends = new HashMap<>();
        for (Integer item : list_A.keySet()) {
            Map<Integer, Integer> buf_Friends_List = getFriendList(item, userActor); // получение друзе для каждого из А
            try {
                TimeUnit.SECONDS.sleep(1/2);
                for (Integer itemBuf : buf_Friends_List.keySet()) {
                    list_Mutual_Friends.put(itemBuf,item);
                }
                haveFriends = haveNMutualFiends(buf_Friends_List,list_B); // сравниваем с тем что есть и добавляем в список
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return list_Mutual_Friends;
    }

    // вернет true, если среди двух Мэпов есть пересечения
    public static boolean haveNMutualFiends(Map<Integer, Integer> list_A, Map<Integer, Integer> list_B){
        boolean haveNMF = false;
        for (Integer itemA : list_A.keySet()) {
            for (Integer itemB : list_B.keySet()) {
                if (itemA.equals(itemB)) {
                    haveNMF = true;
                    System.out.println(itemA + "     " + itemB + "    " + itemA.equals(itemB));
                    break;
                }
            }
        }
        return haveNMF;
    }

    //Вернет объект, авторизированного пользователя
    public static UserActor getUserActor(){
        UserAuthResponse authResponse = null;
        try {
            authResponse = vk.oauth()
                    .userAuthorizationCodeFlow(APP_ID, CLIENT_SECRET, REDIRECT_URI, code)
                    .execute();
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }
        UserActor actorUser = new UserActor(authResponse.getUserId(), authResponse.getAccessToken());
        return actorUser;
    }

    public static UserActor userActor = getUserActor();

    public static void main(String[] args) {
        Integer A = 123772240; // id от кого стоить путь
        Integer B = 0; // id до кого построить путь
        // в структурах ниже хранятся ребра в следуюшем виде [his_friend_id - .keySet();friend_id - .values()]
        Map<Integer,Integer> list_A_0 = getFriendList(A,userActor);
        Map<Integer,Integer> list_B_0 = new HashMap<>();
        list_B_0.put(B,B);
        Map<Integer,Integer> list_A_1 = new HashMap<>();
        Map<Integer,Integer> list_B_1 = new HashMap<>();
        Map<Integer,Integer> list_A_2 = new HashMap<>();
        Map<Integer,Integer> list_B_2 = new HashMap<>();
        Map<Integer,Integer> list_A_3 = new HashMap<>();
        Map<Integer,Integer> list_B_3 = new HashMap<>();
        List<Integer> Path = new ArrayList<>();
        Path.add(A);

        // Так как в теории каждый знаком с каждым через 6,6 человек (6,6/2 = 4) получим 4 списка с каждой стороны
        haveFriends = haveNMutualFiends(list_A_0,list_B_0);
        if(!haveFriends){
            // Формирование нового "уровня" друзей
            list_B_1 = getMutualFriends(list_B_0,list_A_0);
            // Поиск совпадений
        }else{
            Path.add(B);
        }
        if(!haveFriends) {
            // Формирование нового "уровня" друзей
            list_A_1 = getMutualFriends(list_A_0,list_B_1);
        }else{
            boolean flag = false;
            for (Integer itemA : list_A_0.keySet()) {
                for (Integer itemB : list_B_1.keySet()) {
                    if(itemA.equals(itemB)){
                        Path.add(itemA);
                        flag = true;
                        break;
                    }
                }
                if(flag) break;
            }
            Path.add(B);
        }
        if(!haveFriends) {
            // Формирование нового "уровня" друзей
            list_B_2 = getMutualFriends(list_B_1,list_A_1);
        }else{

        }
        if(!haveFriends) {
            // Формирование нового "уровня" друзей
            list_A_2 = getMutualFriends(list_A_1,list_B_2);
        }else{

        }
        if(!haveFriends) {
            // Формирование нового "уровня" друзей
            list_B_3 = getMutualFriends(list_B_2,list_A_2);
        }else{

        }
        if(!haveFriends) {
            // Формирование нового "уровня" друзей
            list_A_3 = getMutualFriends(list_A_2,list_B_3);
        }else{

        }
        if(haveFriends){
            System.out.println("You have path!!!");
            System.out.println(Path);
        }else{
            System.out.println("No path!!!");
        }
    }
}
