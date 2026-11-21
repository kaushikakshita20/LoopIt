//Ishita 18-11-26
package service;
import model.Resource;
import java.util.LinkedHashMap;
import java.util.HashSet;
import model.User;

public class ResourceService {
    private LinkedHashMap<Integer,Resource> resources=new LinkedHashMap<>();
    private HashSet<String> categories=new HashSet<>();
    public boolean addResource(Resource resource,User owner){
        if(resource==null || owner==null){
            System.out.println("Invalid resource or owner!");
            return false;
        }

        if(resources.containsKey(resource.getResourceId())){
            System.out.println("Resource already exists!");
            return false;
        }

        if(resource.getOwnerId()!=owner.getUserId()){
            System.out.println("Invalid resource owner!");
            return false;
        }

        if(resource.getCategory()==null || resource.getCategory().isBlank()){
            System.out.println("Invalid category!");
            return false;
        }

        resources.put(resource.getResourceId(),resource);
        categories.add(resource.getCategory());

        System.out.println("Resource uploaded successfully!");
        return true;

    }
}
