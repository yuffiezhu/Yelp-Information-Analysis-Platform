package yelp_dataAnalysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.io.*;
import org.json.*;

public class readfile {
	public static HashMap<String, Set<String>> readFile(String path) {
		File file = new File(path);
		BufferedReader reader = null;
		HashMap<String, Set<String>> categoryMap = new HashMap<>();
		try{
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			while((tempString = reader.readLine()) != null) {
				JSONObject json = new JSONObject(tempString);
				categoryMap = splitCategory(json, categoryMap);
			}
			reader.close();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if(reader != null) {
				try {
					reader.close();
				} catch(IOException el) {
					el.printStackTrace();
				}
			}
		}
		return categoryMap;
	}

	public static HashMap<String, Set<String>> splitCategory(JSONObject json, HashMap<String, Set<String>> categoryMap) {
		String[] mainArray = {"Active Life", "Arts & Entertainment", "Automotive", "Car Rental", "Cafes", "Beauty & Spas", "Convenience Stores", "Dentists", "Doctors", "Drugstores", "Department Stores", "Education", "Event Planning & Services", "Flowers & Gifts", "Food", "Health & Medical", "Home Services", "Home & Garden", "Hospitals", "Hotels & Travel", "Hardware Stores", "Grocery", "Medical Centers", "Nurseries & Gardening", "Nightlife", "Restaurants", "Shopping", "Transportation"};
		List<String> mainList = Arrays.asList(mainArray);
		JSONArray arr_category = json.getJSONArray("categories");
		int length = arr_category.length();
		List<String> category = new ArrayList<>();
		Set<String> subcategory = new HashSet<>();
		for (int i = 0; i < length; i++) {
			String tempCategory = arr_category.getString(i);
			if (mainList.contains(tempCategory)) {
				category.add(tempCategory);
				if (!categoryMap.containsKey(tempCategory)) {
					categoryMap.put(tempCategory, subcategory);
				} else {
					subcategory.addAll(categoryMap.get(tempCategory));
				}
			} else {
				subcategory.add(tempCategory);
			}
		}
		for (String i : category) {
			categoryMap.put(i, subcategory);
		}
		return categoryMap;
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		HashMap<String, Set<String>> result = new HashMap<>();
		result = readFile(args[0]);
		System.setOut(new PrintStream(new FileOutputStream("output.txt")));
		System.out.println("keys:" + result.keySet());
		int i = 0;
		for (String key : result.keySet()) {
			i = i + 1;
			System.out.println("key" + i + ":" + key);
			System.out.println(result.get(key));
		}
		System.out.println("finish!");
	}
}
