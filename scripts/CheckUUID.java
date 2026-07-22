import java.util.UUID;

public class CheckUUID {
    public static void main(String[] args) {
        System.out.println(UUID.nameUUIDFromBytes("Water Supply".getBytes()));
        System.out.println(UUID.nameUUIDFromBytes("Roads & Traffic".getBytes()));
        System.out.println(UUID.nameUUIDFromBytes("Sanitation & Waste".getBytes()));
        System.out.println(UUID.nameUUIDFromBytes("Electricity".getBytes()));
    }
}
