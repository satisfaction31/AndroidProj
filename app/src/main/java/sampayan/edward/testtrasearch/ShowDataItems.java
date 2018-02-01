package sampayan.edward.testtrasearch;

/**
 * Created by Edward Sampayan.
 */

public class ShowDataItems {
    private String Image_URL,Image_Title, Image_Desc;  //put this name same as Database Fields

    public ShowDataItems(String image_URL, String image_Title, String image_Desc) {
        Image_URL = image_URL;
        Image_Title = image_Title;
        Image_Desc = image_Desc;
    }

    public ShowDataItems()
    {
        //Empty Constructor Needed
    }

    public String getImage_URL() {
        return Image_URL;
    }

    public void setImage_URL(String image_URL) {
        Image_URL = image_URL;
    }

    public String getImage_Title() {
        return Image_Title;
    }

    public void setImage_Title(String image_Title) {
        Image_Title = image_Title;
    }

    public String getImage_Desc() {
        return Image_Desc;
    }

    public void setImage_Desc(String image_Desc) {
        Image_Desc = image_Desc;
    }
}
