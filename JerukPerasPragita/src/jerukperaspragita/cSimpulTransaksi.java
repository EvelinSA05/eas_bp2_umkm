package jerukperaspragita;

// Ini adalah node untuk Linked List Antrean Transaksi
public class cSimpulTransaksi {
    cNota data; // Node ini menyimpan objek cNota
    cSimpulTransaksi next; // Pointer ke node transaksi berikutnya
    
    public cSimpulTransaksi(cNota d) {
        data = d;
        next = null;
    }
}