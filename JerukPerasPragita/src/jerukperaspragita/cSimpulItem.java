package jerukperaspragita;

// Ini adalah node/objek untuk item di dalam keranjang belanja cNota
public class cSimpulItem {
    private cMinuman minuman;
    private int jumlah;
    
    public cSimpulItem(cMinuman minuman, int jumlah) {
        this.minuman = minuman;
        this.jumlah = jumlah;
    }

    public cMinuman getMinuman() {
        return minuman;
    }

    public void setMinuman(cMinuman minuman) {
        this.minuman = minuman;
    }

    public int getJumlah() {
        return jumlah;
    }

    public void setJumlah(int jumlah) {
        this.jumlah = jumlah;
    }
}