import java.util.List;

public class GetIngredientsResponse {
    private boolean success;
    private List<IngredientsAsResponseDataModel> data;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<IngredientsAsResponseDataModel> getData() {
        return data;
    }

    public void setData(List<IngredientsAsResponseDataModel> data) {
        this.data = data;
    }
}
