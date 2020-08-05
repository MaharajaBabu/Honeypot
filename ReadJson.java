/**
 * @Author: Maharaja Babu
 **/

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import org.json.simple.JSONArray; 
import org.json.simple.JSONObject; 
import org.json.simple.parser.*; 


class ReadJson
{
    HashMap<String, String> myMap  = new HashMap<String, String>();

    // Parses JSON file
    public void getRules() throws IOException, ParseException
    {
        Object obj = new JSONParser().parse(new FileReader("rules.json"));
        JSONObject jo = (JSONObject) obj;
        JSONArray temp = (JSONArray) jo.get("rules");
       // System.out.println("MY RULES ARE : "+temp +"\n\n\n");

        String s = temp.toJSONString();
        s = s.substring(1,s.length());
        s = s.substring(1,s.length()-2);
        System.out.println(s);

        String[] pairs = s.split(",");
        for (int i = 0; i < pairs.length-1; i++) {
            String pair = pairs[i];
            String[] keyValue = pair.split(":");
            myMap.put(keyValue[0].substring(1,keyValue[0].length()-1), String.valueOf(keyValue[1]).substring(1, keyValue[1].length()-1));
        }
    }
    /**
     *	@return subnet address
     */
    public String getSubnet()
    {
        return myMap.get("subnet");
    }

    public String getSubnet_r()
    {
        return myMap.get("subnet_r");
    }

    /**
     *	@return IP address
     */
    public String getIPrange()
    {
        return myMap.get("ipRange");
    }

    public String getIPrange_r()
    {
        return myMap.get("ipRange_r");
    }

    /**
     *	@return bridge network name
     */
    public String getName()
    {
        return myMap.get("bridgeNetworkName");
    }

    public String getGateway()
    {
        return myMap.get("gateway");
    }

}