import java.io.Serializable;
import java.util.Date;

public class Artifact implements Serializable{
    private final int id;
    private String name,creator,dateofcreation, genre;

    public Artifact(int id, String name, String creator, String genre){
        this.id = id;
        this.name = name;
        this.creator = creator;
        this.genre = genre;
    }

    public int getId() {
        return id;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public void setCreator(String creator){
        this.creator = creator;
    }

    public String getCreator() {
        return creator;
    }

    public void setDateofcreation(){
        this.dateofcreation = dateofcreation;
    }
    public int getDateofcreation(){
        return Integer.parseInt(dateofcreation);
    }

    public void setGenre(){
        this.dateofcreation = dateofcreation;
    }

    public String getGenre() {
        return genre;
    }

}
