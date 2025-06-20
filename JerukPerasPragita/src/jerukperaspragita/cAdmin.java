package jerukperaspragita;

/**
 *
 * @author EVELIN
 */
public class cAdmin extends cOrang {
    private String hakAkses;

    public cAdmin() {  //default konstruktor
        super("", "", "", "", ""); // Panggil konstruktor parent dengan 5 parameter
        this.hakAkses = "";
    }

    public cAdmin(String id, String nama, String email, String password, String ha) { //parameter rized
        super(id, nama, password, email, null); // Telp default null untuk Admin
        this.hakAkses = ha;
    }

    public void tampilkanInfo() {
        System.out.println("\nID Admin         : " + getId());
        System.out.println("Nama Admin       : " + getNama());
        System.out.println("Email            : " + getEmail());
        System.out.println("Hak Akses        : " + hakAkses);
        System.out.println("Telepon          : " + (getTelp() != null ? getTelp() : "-")); // Tampilkan telepon
    }

    public void setHakAkses(String ha) {
        hakAkses = ha;
    }

    public String getHakAkses() {
        return hakAkses;
    }

    //to String
    @Override
    public String toString() {
        return "ID Admin                    : " + id
                + "\nNama Admin                  : " + nama
                + "\nEmail                       : " + email
                + "\nHak Akses                   : " + hakAkses
                + "\nTelepon                     : " + (telp != null ? telp : "-");
    }
}