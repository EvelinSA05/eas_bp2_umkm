package jerukperaspragita;

import java.util.ArrayList;
import java.util.Objects;

public class cNota {
    private String kode;
    private cPembeli pembeli;
    private ArrayList<cSimpulItem> keranjang;
    private int status; // 0: Pending, 1: Selesai, 2: Dibatalkan

    public cNota(String kode, cPembeli p) {
        this.kode = kode;
        this.pembeli = p;
        this.keranjang = new ArrayList<>();
        this.status = 0; // Default status pending
    }

    public void tambahBarang(cMinuman minuman, int jumlah) {
        // Cek apakah barang sudah ada di keranjang, jika ya, update jumlahnya
        for (cSimpulItem itemKeranjang : keranjang) {
            if (itemKeranjang.getMinuman().getId() == minuman.getId()) {
                itemKeranjang.setJumlah(itemKeranjang.getJumlah() + jumlah);
                System.out.println(minuman.getNama() + " berhasil ditambahkan (jumlah diperbarui).");
                return;
            }
        }
        // Jika belum ada, tambahkan sebagai item baru
        keranjang.add(new cSimpulItem(minuman, jumlah));
        System.out.println(minuman.getNama() + " berhasil ditambahkan.");
    }
    
    // Metode untuk menghapus barang tertentu dengan jumlah tertentu
    public void hapusBarangDariKeranjang(int idBarang, int jumlahHapus) {
        cSimpulItem itemDitemukan = null;
        for (cSimpulItem item : keranjang) {
            if (item.getMinuman().getId() == idBarang) {
                itemDitemukan = item;
                break;
            }
        }

        if (itemDitemukan != null) {
            if (jumlahHapus >= itemDitemukan.getJumlah()) {
                // Hapus seluruh item jika jumlah yang diminta lebih besar atau sama
                keranjang.remove(itemDitemukan);
                System.out.println("Semua " + itemDitemukan.getMinuman().getNama() + " berhasil dihapus dari keranjang.");
            } else {
                // Kurangi jumlah item
                itemDitemukan.setJumlah(itemDitemukan.getJumlah() - jumlahHapus);
                System.out.println(jumlahHapus + " " + itemDitemukan.getMinuman().getNama() + " berhasil dikurangi dari keranjang.");
            }
        } else {
            System.out.println("Barang tidak ditemukan di keranjang Anda.");
        }
    }

    public void lihatNota() {
        System.out.println("\n--- Detail Transaksi: " + kode + " ---");
        System.out.println("Pembeli: " + pembeli.getNama() + (pembeli.getKodeMember().equals("Non-Member") ? "" : " (Member ID: " + pembeli.getId() + ")"));
        System.out.println("Status: " + (status == 0 ? "Pending" : status == 1 ? "Selesai" : "Dibatalkan"));
        System.out.println("-------------------------------------------------------");
        System.out.printf("%-5s %-25s %-10s %-15s %-15s\n", "No.", "Nama Barang", "Jumlah", "Harga Satuan", "Subtotal");
        System.out.println("-------------------------------------------------------");

        if (keranjang.isEmpty()) {
            System.out.println("Keranjang kosong.");
        } else {
            int i = 1;
            for (cSimpulItem s : keranjang) {
                double subtotal = (double)s.getMinuman().getHarga() * s.getJumlah();
                System.out.printf(" %d. %-25s %-10d Rp%,-12d Rp%,-12.0f\n",
                    (i++), s.getMinuman().getNama(), s.getJumlah(), s.getMinuman().getHarga(), subtotal);
            }
        }
        System.out.println("-------------------------------------------------------");
        System.out.printf("%-55s Rp%,-12.0f\n", "TOTAL:", hitungTotal());
        System.out.println("-------------------------------------------------------");
    }
    
    public double hitungTotal() {
        double grandTotal = 0;
        for (cSimpulItem s : keranjang) {
            grandTotal += (double)s.getMinuman().getHarga() * s.getJumlah();
        }
        return grandTotal;
    }
    
    // Getters dan Setters
    public String getKode() { return kode; }
    public cPembeli getPembeli() { return pembeli; }
    public int getStatus() { return status; }
    public void setStatus(int s) { status = s; }
    public ArrayList<cSimpulItem> getKeranjang() { return keranjang; } // Mengembalikan ArrayList

    public int getJumlahBarang() {
        return keranjang.size();
    }

    public cMinuman getMinuman(int index) { // Ambil objek cMinuman dari index di keranjang
        if (index >= 0 && index < keranjang.size()) {
            return keranjang.get(index).getMinuman();
        }
        return null;
    }

    public int getJumlah(int index) { // Ambil jumlah dari item di index keranjang
        if (index >= 0 && index < keranjang.size()) {
            return keranjang.get(index).getJumlah();
        }
        return 0;
    }

    // Tambahkan metode equals dan hashCode agar bisa digunakan di LinkedList (untuk remove)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        cNota cNota = (cNota) o;
        return Objects.equals(kode, cNota.kode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kode);
    }
}