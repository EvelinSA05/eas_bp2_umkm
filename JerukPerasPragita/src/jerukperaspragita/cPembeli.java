package jerukperaspragita;

public class cPembeli extends cOrang {
    private String kodeMember; // Mengubah nama dari 'kode' menjadi 'kodeMember' untuk menghindari ambiguitas dengan 'kode' di cNota
    private double totalBelanja;

    public cPembeli() {
        super("", "", "", "", ""); // Panggil konstruktor parent
        this.kodeMember = "";
    }
    
    // Constructor untuk non-member
    public cPembeli(String nama) {
        super(null, nama, null, null, null); // Non-member tidak punya ID, password, email, telp dari DB
        this.kodeMember = "Non-Member"; // Kode khusus untuk menandai non-member
        this.totalBelanja = 0;
    }

    // Constructor untuk member yang diambil dari database
    public cPembeli(String id, String nama, String password, String telp, String kodeMember) {
        super(id, nama, password, null, telp); // Email diatur null, nanti bisa di-load dari DB
        this.kodeMember = kodeMember;
        this.totalBelanja = 0; // Total belanja akan di-load dari DB atau diupdate
    }

    // Tambahan: Constructor jika email juga ada untuk member
    public cPembeli(String id, String nama, String email, String password, String telp, String kodeMember) {
        super(id, nama, password, email, telp);
        this.kodeMember = kodeMember;
        this.totalBelanja = 0; // Total belanja akan di-load dari DB atau diupdate
    }

    public void tambahTotalBelanja(double jumlah) {
        this.totalBelanja += jumlah;
    }
    
    public double getTotalBelanja() {
        return totalBelanja;
    }

    // Mengganti getKode() menjadi getKodeMember() untuk kejelasan
    public String getKodeMember() {
        return kodeMember;
    }
    
    public void setKodeMember(String kodeMember) {
        this.kodeMember = kodeMember;
    }

    public void setTotalBelanja(double totalBelanja) { // Setter untuk memuat dari DB
        this.totalBelanja = totalBelanja;
    }

    @Override
    public String getId() {
        return super.getId();
    }
}