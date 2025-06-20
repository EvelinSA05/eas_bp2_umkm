package jerukperaspragita;

/**
 *
 * @author EVELIN
 */
public class cOrang {

    public String id;
    public String nama;
    public String password;
    public String email; // Ditambah untuk email
    public String telp;  // Ditambah untuk telepon

    // Constructor utama yang mencakup semua atribut
    public cOrang(String id, String nama, String password, String email, String telp) {
        this.id = id;
        this.nama = nama;
        this.password = password;
        this.email = email;
        this.telp = telp;
    }

    // Constructor yang Anda gunakan sebelumnya, akan diubah agar tetap konsisten
    public cOrang(String id, String nama, String password, String email) {
        this(id, nama, password, email, null); // Telepon default null
    }

    // Constructor untuk non-member (hanya nama)
    public cOrang(String nama) {
        this(null, nama, null, null, null); // ID, password, email, telepon default null
    }

    // Getter dan Setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelp() {
        return telp;
    }

    public void setTelp(String telp) {
        this.telp = telp;
    }

    // Metode untuk login (digunakan oleh Admin dan Owner)
    public boolean cocokLogin(String emailInput, String passwordInput) {
        // Cek jika email tidak null dan cocok
        return this.email != null && this.email.equalsIgnoreCase(emailInput) && this.password.equals(passwordInput);
    }
}