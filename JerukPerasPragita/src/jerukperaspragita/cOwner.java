package jerukperaspragita;

/**
 *
 * @author EVELIN
 */
public class cOwner extends cOrang {

    // Konstruktor tanpa parameter (default)
    public cOwner() {
        super("", "", "", "", ""); // Memanggil konstruktor parent dengan nilai default
    }

    public cOwner(String id, String nama, String email, String password) { //parameterized
        super(id, nama, password, email, null); // Telp default null untuk Owner
    }

    //setter
    public void tampilkanInfo() {
        System.out.println("ID Owner         : " + getId());
        System.out.println("Nama Owner       : " + getNama());
        System.out.println("Email Owner      : " + getEmail());
        System.out.println("Telepon          : " + (getTelp() != null ? getTelp() : "-"));
        System.out.println("------------------------------");
    }
}