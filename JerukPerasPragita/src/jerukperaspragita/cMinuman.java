package jerukperaspragita;

/**
 *
 * @author EVELIN
 */
public class cMinuman {

    private int id; // Tambahkan atribut ID untuk barang dari database
    public String nama;
    public int harga;
    public int stok;

    // Constructor baru yang mencakup ID (dari database)
    public cMinuman(int id, String nama, int harga, int stok) {
        this.id = id;
        this.nama = nama;
        this.harga = harga;
        this.stok = stok;
    }

    // Constructor lama yang Anda punya, untuk kompatibilitas jika masih dipakai di tempat lain
    // (Misalnya saat inisialisasi awal tanpa ID dari DB, meskipun nanti akan di-load dari DB)
    public cMinuman(String nama, int harga, int stok) {
        this(0, nama, harga, stok); // ID default 0 jika tidak ada dari DB
    }

    public cMinuman() {
        // constructor kosong
    }

    //setter
    public void tampilkanInfo() {
        System.out.println("\nID      : " + getId()); // Tampilkan ID
        System.out.println("Nama    : " + getNama());
        System.out.println("Harga   : " + getHarga());
        System.out.println("Stok    : " + getStok());
    }

    // Getter dan Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public int getHarga() {
        return harga;
    }

    public void setHarga(int harga) {
        this.harga = harga;
    }

    public int getStok() {
        return stok;
    }

    public void setStok(int stok) {
        this.stok = stok;
    }

    @Override
    public String toString() {
        return "ID Minuman   : " + id
                + "\nNama Minuman : " + nama
                + "\nHarga        : " + harga
                + "\nStok         : " + stok;
    }
}