package comp.cap.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.cap.domain.User;

import comp.cap.util.Encryption;

public class UserDAOImp implements UserDAO {

	List<User> User;
	static AmazonDynamoDB dynamoDB;
	
	private static String tableName = "user";
	
	public UserDAOImp(){
		 dynamoDB = AmazonDynamoDBClientBuilder.standard()
	                .withRegion(Regions.EU_WEST_1)
	                .withCredentials(new ProfileCredentialsProvider("default"))
	                .build();
	}
	
	@Override
	public List<User> getUsers() {
		List<User> users = new ArrayList<User>();
		
		ScanRequest scanRequest = new ScanRequest(tableName);
        ScanResult scanResult = dynamoDB.scan(scanRequest);
        for(Map<String, AttributeValue> userMap: scanResult.getItems()){
        	users.add(new User(userMap.get("FirstName").getS(),
   		 		 			   userMap.get("LastName").getS(),
   		 		 			   userMap.get("email").getS(),
   		 		 			   userMap.get("password").getS()));
        }
        System.out.println("users Result: " + users);
		return users;
	}

	@Override
	public User getUser(String email){
		 HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
         Condition condition = new Condition().withComparisonOperator(ComparisonOperator.EQ)
        		 							  .withAttributeValueList(new AttributeValue().withS(email));
         scanFilter.put("email", condition);
         ScanRequest scanRequest = new ScanRequest(tableName).withScanFilter(scanFilter);
         ScanResult scanResult = dynamoDB.scan(scanRequest);
         if(scanResult.getCount()!=1){
        	 try {
				throw new Exception("user not found ");
			} catch (Exception e) {
				e.printStackTrace();
			}
         }
			
         
         Map<String, AttributeValue> userMap = scanResult.getItems().get(0);
         scanResult.getItems().forEach(map -> System.out.println("user result :" + userMap));
         userMap.forEach((k,v) -> System.out.println(k + ":" + v.getS()));
         
         return new User(userMap.get("FirstName").getS(),
        		 		 userMap.get("LastName").getS(),
        		 		 userMap.get("email").getS(),
        		 		 userMap.get("password").getS());
	}

	@Override
	public void updateUser(User user) {
		// TODO Auto-generated method stub
	}

	@Override
	public void deleteUSer(User user) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addUser(com.cap.domain.User user) {
		// Add an item
        Map<String, AttributeValue> item = user(user);
        PutItemRequest putItemRequest = new PutItemRequest(tableName, item);
        PutItemResult putItemResult = dynamoDB.putItem(putItemRequest);
        System.out.println("putItemResult : "+ putItemResult);
	}
	
	private static Map<String, AttributeValue> user(User user) {
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
        item.put("FirstName", new AttributeValue(user.getFirstName()));
        item.put("LastName", new AttributeValue(user.getLastName()));
        item.put("email", new AttributeValue(user.getEmail()));
        item.put("password", new AttributeValue(Encryption.cryptWithMD5(user.getPassword())));
        return item;
    }
	

}
