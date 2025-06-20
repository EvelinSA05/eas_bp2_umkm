package jerukperaspragita;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;

public class JerukPerasPragita {

    private static Connection conn;
    private static Scanner sc = new Scanner(System.in);
    private static ArrayList<cAdmin> daftarAdmin = new ArrayList<>();
    private static ArrayList<cPembeli> daftarPembeli = new ArrayList<>();
    private static ArrayList<cOwner> daftarOwner = new ArrayList<>();
    private static ArrayList<cMinuman> daftarMinuman = new ArrayList<>();

    private static LinkedList<cNota> antreanTransaksi = new LinkedList<>();
    private static int counterTransaksi = 1;

    public static void main(String[] args) {
        conn = cKoneksi.getKoneksi();
        if (conn == null) {
            System.err.println("Aplikasi tidak dapat terhubung ke database. Pastikan database berjalan dan konfigurasi koneksi benar.");
            return;
        }

        loadMasterData();
        loadPendingTransactions();

        while (true) {
            System.out.println("\n===== Aplikasi Penjualan Jeruk Peras =====");
            System.out.println("1. Pembeli (Non-Member)");
            System.out.println("2. Member");
            System.out.println("3. Admin");
            System.out.println("4. Pemilik");
            System.out.println("5. Keluar");
            System.out.print("Pilihan: ");
            String pilihan = sc.nextLine();

            switch (pilihan) {
                case "1":
                    MenuPembeli.menu(false, null);
                    break;
                case "2":
                    MenuPembeli.loginMember();
                    break;
                case "3":
                    MenuAdmin.login();
                    break;
                case "4":
                    MenuOwner.login();
                    break;
                case "5":
                    System.out.println("Terima kasih telah menggunakan aplikasi ini.");
                    cKoneksi.closeKoneksi();
                    sc.close();
                    System.exit(0);
                default:
                    System.out.println("Pilihan tidak valid! Silakan masukkan angka 1-5.");
            }
        }
    }

    private static void loadMasterData() {
        try (Statement s = conn.createStatement()) {
            daftarAdmin.clear();
            daftarPembeli.clear();
            daftarOwner.clear();
            daftarMinuman.clear();

            ResultSet rsPengguna = s.executeQuery("SELECT id_pengguna, nama_pengguna, password, email, telepon, peran, total_belanja FROM pengguna");
            while (rsPengguna.next()) {
                String id = rsPengguna.getString("id_pengguna");
                String nama = rsPengguna.getString("nama_pengguna");
                String pass = rsPengguna.getString("password");
                String email = rsPengguna.getString("email");
                String telp = rsPengguna.getString("telepon");
                String peran = rsPengguna.getString("peran");

                if (peran.equalsIgnoreCase("member")) {
                    cPembeli p = new cPembeli(id, nama, email, pass, telp, id);
                    p.setTotalBelanja(rsPengguna.getDouble("total_belanja"));
                    daftarPembeli.add(p);
                } else if (peran.equalsIgnoreCase("admin")) {
                    daftarAdmin.add(new cAdmin(id, nama, email, pass, null));
                } else if (peran.equalsIgnoreCase("pemilik")) {
                    daftarOwner.add(new cOwner(id, nama, email, pass));
                }
            }

            ResultSet rsBarang = s.executeQuery("SELECT id_barang, nama_barang, harga, stok FROM barang");
            while (rsBarang.next()) {
                daftarMinuman.add(new cMinuman(rsBarang.getInt("id_barang"), rsBarang.getString("nama_barang"), rsBarang.getInt("harga"), rsBarang.getInt("stok")));
            }
            System.out.println("Master data berhasil dimuat/disegarkan.");
        } catch (SQLException e) {
            System.err.println("Gagal memuat master data: " + e.getMessage());
        }
    }

    private static void loadPendingTransactions() {
        antreanTransaksi.clear();
        String sql = "SELECT t.kode_transaksi, t.id_pengguna, t.nama_non_member, t.tanggal, t.status, " +
                     "p.nama_pengguna, p.password, p.telepon, p.email " +
                     "FROM transaksi t " +
                     "LEFT JOIN pengguna p ON t.id_pengguna = p.id_pengguna " +
                     "WHERE t.status = 0";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String kodeTransaksi = rs.getString("kode_transaksi");
                String idPengguna = rs.getString("id_pengguna");
                String namaNonMember = rs.getString("nama_non_member");

                cPembeli pembeliNota;
                if (idPengguna != null) {
                    pembeliNota = new cPembeli(idPengguna, rs.getString("nama_pengguna"), rs.getString("email"), rs.getString("password"), rs.getString("telepon"), idPengguna);
                } else {
                    pembeliNota = new cPembeli(namaNonMember);
                }

                cNota nota = new cNota(kodeTransaksi, pembeliNota);
                nota.setStatus(rs.getInt("status"));

                String detailSql = "SELECT dt.id_barang, dt.jumlah, dt.harga_satuan_saat_transaksi, b.nama_barang, b.harga AS harga_asli_barang, b.stok " +
                                   "FROM detail_transaksi dt JOIN barang b ON dt.id_barang = b.id_barang " +
                                   "WHERE dt.kode_transaksi = ?";
                try (PreparedStatement psDetail = conn.prepareStatement(detailSql)) {
                    psDetail.setString(1, kodeTransaksi);
                    ResultSet rsDetail = psDetail.executeQuery();
                    while (rsDetail.next()) {
                        int idBarang = rsDetail.getInt("id_barang");
                        String namaBarang = rsDetail.getString("nama_barang");
                        int hargaSatuanSaatTransaksi = rsDetail.getInt("harga_satuan_saat_transaksi");
                        int jumlah = rsDetail.getInt("jumlah");
                        cMinuman itemInNota = new cMinuman(idBarang, namaBarang, hargaSatuanSaatTransaksi, 0);
                        nota.tambahBarang(itemInNota, jumlah);
                    }
                }
                antreanTransaksi.add(nota);
            }
            System.out.println("Transaksi pending berhasil dimuat: " + antreanTransaksi.size() + " transaksi.");
        } catch (SQLException e) {
            System.err.println("Gagal memuat transaksi pending: " + e.getMessage());
        }
    }

    private static ArrayList<cMinuman> getSemuaMinumanDatabase() {
        daftarMinuman.clear();
        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery("SELECT id_barang, nama_barang, harga, stok FROM barang")) {
            while (rs.next()) {
                daftarMinuman.add(new cMinuman(rs.getInt("id_barang"), rs.getString("nama_barang"), rs.getInt("harga"), rs.getInt("stok")));
            }
        } catch (SQLException e) {
            System.err.println("Gagal memuat daftar minuman dari database: " + e.getMessage());
        }
        return daftarMinuman;
    }

    static class MenuPembeli {
        public static void loginMember() {
            System.out.print("ID Member: ");
            String id = sc.nextLine();
            System.out.print("Password: ");
            String pass = sc.nextLine();

            cPembeli member = null;
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM pengguna WHERE id_pengguna = ? AND password = ? AND peran = 'member'")) {
                ps.setString(1, id);
                ps.setString(2, pass);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    member = new cPembeli(rs.getString("id_pengguna"), rs.getString("nama_pengguna"), rs.getString("email"), rs.getString("password"), rs.getString("telepon"), rs.getString("id_pengguna"));
                    member.setTotalBelanja(rs.getDouble("total_belanja"));
                }
            } catch (SQLException e) {
                System.err.println("Error saat login member: " + e.getMessage());
            }

            if (member != null) {
                System.out.println("Login berhasil sebagai " + member.getNama());
                menu(true, member);
            } else {
                System.out.println("Login Gagal! ID Member atau Password salah.");
            }
        }

        public static void menu(boolean isMember, cPembeli dataMember) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyMMddHHmmss");
            String kodeTransaksiBaru = "TRX-" + dtf.format(LocalDateTime.now()) + String.format("%03d", counterTransaksi++);

            cPembeli pembeli;
            if (isMember) {
                pembeli = dataMember;
                System.out.println("\nSelamat Datang Kembali, " + pembeli.getNama() + " (Member)");
            } else {
                System.out.print("Masukkan Nama Anda: ");
                String nama = sc.nextLine();
                pembeli = new cPembeli(nama);
            }

            cNota notaBaru = new cNota(kodeTransaksiBaru, pembeli);

            boolean selesai = false;
            while (!selesai) {
                System.out.println("\n--- Keranjang Belanja [" + kodeTransaksiBaru + "] ---");
                System.out.println("1. Tambah Barang");
                System.out.println("2. Hapus Barang dari Keranjang");
                System.out.println("3. Lihat Keranjang");
                System.out.println("4. Selesai");
                if (isMember) System.out.println("5. Ubah Password");
                System.out.println("0. Kembali ke Menu Utama");

                System.out.print("Pilihan: ");
                String pilihan = sc.nextLine();
                int choice;
                try {
                    choice = Integer.parseInt(pilihan);
                }
                catch (NumberFormatException e) {
                    System.out.println("Pilihan tidak valid! Masukkan angka.");
                    continue;
                }

                switch (choice) {
                    case 1:
                        ArrayList<cMinuman> semuaMinuman = getSemuaMinumanDatabase();
                        if (semuaMinuman.isEmpty()) {
                            System.out.println("Tidak ada barang tersedia saat ini.");
                            break;
                        }
                        System.out.println("\n--- Daftar Barang ---");
                        System.out.printf("%-5s %-25s %-15s %-10s\n", "No.", "Nama Barang", "Harga", "Stok");
                        System.out.println("----------------------------------------------------------");
                        for (int i = 0; i < semuaMinuman.size(); i++) {
                            cMinuman m = semuaMinuman.get(i);
                            int hargaTampil = isMember ? (int) (m.getHarga() * 0.95) : m.getHarga();
                            System.out.printf("%-5d %-25s Rp%,-12d %-10d\n", (i + 1), m.getNama(), hargaTampil, m.getStok());
                        }

                        System.out.print("Pilih Nomor Barang (0 untuk batal): ");
                        int idx;
                        try {
                            idx = sc.nextInt();
                            sc.nextLine();
                        }
                        catch (InputMismatchException e) {
                            System.out.println("Input tidak valid! Masukkan angka.");
                            sc.nextLine();
                            break;
                        }

                        if (idx == 0) {
                            System.out.println("Penambahan barang dibatalkan.");
                            break;
                        }

                        if (idx > 0 && idx <= semuaMinuman.size()) {
                            cMinuman minumanDipilih = semuaMinuman.get(idx - 1);
                            System.out.print("Masukkan Jumlah: ");
                            int jumlah;
                            try {
                                jumlah = sc.nextInt();
                                sc.nextLine();
                            }
                            catch (InputMismatchException e) {
                                System.out.println("Input tidak valid! Masukkan angka.");
                                sc.nextLine();
                                break;
                            }

                            if (jumlah <= 0) {
                                System.out.println("Jumlah harus lebih dari 0.");
                                break;
                            }

                            int currentStok = 0;
                            try (PreparedStatement ps = conn.prepareStatement("SELECT stok FROM barang WHERE id_barang = ?")) {
                                ps.setInt(1, minumanDipilih.getId());
                                ResultSet rs = ps.executeQuery();
                                if (rs.next()) {
                                    currentStok = rs.getInt("stok");
                                }
                            }
                            catch (SQLException e) {
                                System.err.println("Error saat mengecek stok: " + e.getMessage());
                                break;
                            }

                            if (currentStok < jumlah) {
                                System.out.println("Stok " + minumanDipilih.getNama() + " tidak cukup! Stok tersedia: " + currentStok);
                                break;
                            }

                            cMinuman itemUntukNota;
                            if (isMember) {
                                int hargaDiskon = (int) (minumanDipilih.getHarga() * 0.95);
                                itemUntukNota = new cMinuman(minumanDipilih.getId(), minumanDipilih.getNama(), hargaDiskon, 0);
                            } else {
                                itemUntukNota = new cMinuman(minumanDipilih.getId(), minumanDipilih.getNama(), minumanDipilih.getHarga(), 0);
                            }
                            notaBaru.tambahBarang(itemUntukNota, jumlah);
                        } else {
                            System.out.println("Pilihan nomor barang tidak valid!");
                        }
                        break;
                    case 2:
                        if (notaBaru.getJumlahBarang() == 0) {
                            System.out.println("Keranjang kosong! Tidak ada yang bisa dihapus.");
                        } else {
                            notaBaru.lihatNota();
                            System.out.print("Pilih nomor urut barang di keranjang yang ingin dihapus/dikurangi (0 untuk batal): ");
                            int nomorUrutBarang;
                            try {
                                nomorUrutBarang = sc.nextInt();
                                sc.nextLine();
                            }
                            catch (InputMismatchException e) {
                                System.out.println("Input tidak valid! Masukkan angka.");
                                sc.nextLine();
                                break;
                            }

                            if (nomorUrutBarang == 0) {
                                System.out.println("Pembatalan penghapusan barang.");
                                break;
                            }

                            if (nomorUrutBarang > 0 && nomorUrutBarang <= notaBaru.getJumlahBarang()) {
                                cMinuman itemYangAkanDihapus = notaBaru.getMinuman(nomorUrutBarang - 1);
                                System.out.print("Masukkan jumlah yang ingin dihapus/dikurangi: ");
                                int jumlahHapus;
                                try {
                                    jumlahHapus = sc.nextInt();
                                    sc.nextLine();
                                }
                                catch (InputMismatchException e) {
                                    System.out.println("Input tidak valid! Masukkan angka.");
                                    sc.nextLine();
                                    break;
                                }

                                if (jumlahHapus <= 0) {
                                    System.out.println("Jumlah harus lebih dari 0.");
                                    break;
                                }
                                
                                notaBaru.hapusBarangDariKeranjang(itemYangAkanDihapus.getId(), jumlahHapus);

                            } else {
                                System.out.println("Nomor urut barang tidak valid.");
                            }
                        }
                        break;
                    case 3:
                        notaBaru.lihatNota();
                        break;
                    case 4:
                        if (notaBaru.getJumlahBarang() > 0) {
                            simpanTransaksiKeDB(notaBaru);
                            selesai = true;
                        } else {
                            System.out.println("Keranjang kosong, tidak dapat checkout.");
                        }
                        break;
                    case 5:
                        if (isMember) {
                            ubahPassword(dataMember);
                        } else {
                            System.out.println("Pilihan tidak valid!");
                        }
                        break;
                    case 0:
                        if (notaBaru.getJumlahBarang() > 0) {
                            System.out.print("Anda memiliki item di keranjang. Transaksi tidak akan disimpan. Lanjutkan? (y/n): ");
                            String konfirmasi = sc.nextLine();
                            if (!konfirmasi.equalsIgnoreCase("y")) {
                                break;
                            }
                        }
                        selesai = true;
                        break;
                    default:
                        System.out.println("Pilihan tidak valid! Silakan pilih opsi yang tersedia.");
                }
            }
        }

        private static void ubahPassword(cPembeli member) {
            System.out.print("Masukkan Password Lama: ");
            String oldPass = sc.nextLine();

            if (!member.getPassword().equals(oldPass)) {
                System.out.println("Password lama salah!");
                return;
            }

            System.out.print("Masukkan Password Baru: ");
            String newPass = sc.nextLine();
            System.out.print("Konfirmasi Password Baru: ");
            String confirmPass = sc.nextLine();

            if (!newPass.equals(confirmPass)) {
                System.out.println("Konfirmasi password tidak cocok!");
                return;
            }

            try (PreparedStatement ps = conn.prepareStatement("UPDATE pengguna SET password = ? WHERE id_pengguna = ?")) {
                ps.setString(1, newPass);
                ps.setString(2, member.getId());
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    member.setPassword(newPass);
                    System.out.println("Password berhasil diubah.");
                } else {
                    System.out.println("Gagal mengubah password.");
                }
            } catch (SQLException e) {
                System.err.println("Error saat mengubah password: " + e.getMessage());
            }
        }

        private static void simpanTransaksiKeDB(cNota nota) {
            String sql_transaksi = "INSERT INTO transaksi (kode_transaksi, id_pengguna, nama_non_member, tanggal, status, total_akhir) VALUES (?, ?, ?, ?, ?, ?)";
            String sql_detail = "INSERT INTO detail_transaksi (kode_transaksi, id_barang, jumlah, harga_satuan_saat_transaksi) VALUES (?, ?, ?, ?)";
            String sql_update_stok = "UPDATE barang SET stok = stok - ? WHERE id_barang = ?";
            String sql_update_total_belanja_member = "UPDATE pengguna SET total_belanja = total_belanja + ? WHERE id_pengguna = ?";

            try {
                conn.setAutoCommit(false);

                double totalNotaUntukMember = 0;

                try (PreparedStatement psTransaksi = conn.prepareStatement(sql_transaksi)) {
                    psTransaksi.setString(1, nota.getKode());
                    if (nota.getPembeli().getKodeMember().equals("Non-Member")) {
                        psTransaksi.setNull(2, Types.VARCHAR);
                        psTransaksi.setString(3, nota.getPembeli().getNama());
                    } else {
                        psTransaksi.setString(2, nota.getPembeli().getId());
                        psTransaksi.setNull(3, Types.VARCHAR);
                    }
                    psTransaksi.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
                    psTransaksi.setInt(5, 0);
                    psTransaksi.setDouble(6, nota.hitungTotal());
                    psTransaksi.executeUpdate();
                }

                try (PreparedStatement psDetail = conn.prepareStatement(sql_detail);
                     PreparedStatement psStok = conn.prepareStatement(sql_update_stok)) {

                    for (cSimpulItem itemKeranjang : nota.getKeranjang()) {
                        cMinuman itemMinuman = itemKeranjang.getMinuman();
                        int jumlahItem = itemKeranjang.getJumlah();

                        int currentStok = 0;
                        try (PreparedStatement psCheckStok = conn.prepareStatement("SELECT stok FROM barang WHERE id_barang = ?")) {
                            psCheckStok.setInt(1, itemMinuman.getId());
                            ResultSet rsStok = psCheckStok.executeQuery();
                            if (rsStok.next()) {
                                currentStok = rsStok.getInt("stok");
                            }
                        }
                        if (currentStok < jumlahItem) {
                            throw new SQLException("Stok untuk " + itemMinuman.getNama() + " tidak cukup saat checkout. Transaksi dibatalkan.");
                        }

                        psDetail.setString(1, nota.getKode());
                        psDetail.setInt(2, itemMinuman.getId());
                        psDetail.setInt(3, jumlahItem);
                        psDetail.setInt(4, itemMinuman.getHarga());
                        psDetail.addBatch();

                        psStok.setInt(1, jumlahItem);
                        psStok.setInt(2, itemMinuman.getId());
                        psStok.addBatch();

                        totalNotaUntukMember += (double)itemMinuman.getHarga() * jumlahItem;
                    }
                    psDetail.executeBatch();
                    psStok.executeBatch();
                }

                if (!nota.getPembeli().getKodeMember().equals("Non-Member")) {
                    try (PreparedStatement psUpdateMember = conn.prepareStatement(sql_update_total_belanja_member)) {
                        psUpdateMember.setDouble(1, totalNotaUntukMember);
                        psUpdateMember.setString(2, nota.getPembeli().getId());
                        psUpdateMember.executeUpdate();
                        nota.getPembeli().tambahTotalBelanja(totalNotaUntukMember);
                    }
                }

                conn.commit();
                System.out.println("Transaksi berhasil disimpan. Menunggu diproses oleh Admin.");
                antreanTransaksi.add(nota);
                loadMasterData();

            } catch (SQLException e) {
                System.err.println("Gagal menyimpan transaksi: " + e.getMessage());
                try {
                    conn.rollback();
                    System.out.println("Transaksi dibatalkan karena kesalahan: " + e.getMessage());
                } catch (SQLException ex) {
                    System.err.println("Error saat rollback: " + ex.getMessage());
                }
            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ex) {
                    System.err.println("Error saat mengembalikan auto-commit: " + ex.getMessage());
                }
            }
        }
    }

    static class MenuAdmin {
        public static void login() {
            System.out.print("Email Admin: ");
            String email = sc.nextLine();
            System.out.print("Password: ");
            String pw = sc.nextLine();
            try (PreparedStatement ps = conn.prepareStatement("SELECT id_pengguna, nama_pengguna, email, telepon FROM pengguna WHERE email = ? AND password = ? AND peran = 'admin'")) {
                ps.setString(1, email);
                ps.setString(2, pw);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    System.out.println("\nLogin berhasil sebagai " + rs.getString("nama_pengguna"));
                    menu();
                } else {
                    System.out.println("Login Gagal! Email atau Password salah.");
                }
            } catch (SQLException e) {
                System.err.println("Error saat login admin: " + e.getMessage());
            }
        }

        public static void menu() {
            boolean logout = false;
            while (!logout) {
                int belumDiproses = 0;
                for (cNota nota : antreanTransaksi) {
                    if (nota.getStatus() == 0) {
                        belumDiproses++;
                    }
                }

                System.out.println("\n--- Menu Admin ---");
                System.out.println(belumDiproses + " transaksi belum diproses.");
                System.out.println("1. Proses Transaksi");
                System.out.println("2. Tampilkan Transaksi Belum Diproses");
                System.out.println("0. Logout");
                System.out.print("Pilihan: ");
                String pilihan = sc.nextLine();

                switch (pilihan) {
                    case "1":
                        prosesTransaksi();
                        break;
                    case "2":
                        tampilkanTransaksiBelumDiproses();
                        break;
                    case "0":
                        logout = true;
                        System.out.println("Anda telah logout dari Admin.");
                        break;
                    default:
                        System.out.println("Pilihan tidak valid!");
                }
            }
        }

        private static void prosesTransaksi() {
            if (antreanTransaksi.isEmpty()) {
                System.out.println("Tidak ada transaksi dalam antrean.");
                return;
            }

            ArrayList<cNota> pendingNotes = new ArrayList<>();
            for (cNota nota : antreanTransaksi) {
                if (nota.getStatus() == 0) {
                    pendingNotes.add(nota);
                }
            }

            if (pendingNotes.isEmpty()) {
                System.out.println("Tidak ada transaksi pending untuk diproses.");
                return;
            }

            System.out.println("\n--- Transaksi Belum Diproses ---");
            for (int i = 0; i < pendingNotes.size(); i++) {
                cNota n = pendingNotes.get(i);
                System.out.println((i + 1) + ". Kode: " + n.getKode() + " | Pembeli: " + n.getPembeli().getNama() + " | Total: Rp" + String.format("%,.0f", n.hitungTotal()));
            }

            System.out.print("Pilih nomor transaksi yang akan diproses (0 untuk batal): ");
            int choice;
            try {
                choice = sc.nextInt();
                sc.nextLine();
            } catch (InputMismatchException e) {
                System.out.println("Input tidak valid! Masukkan angka.");
                sc.nextLine();
                return;
            }

            if (choice == 0) {
                System.out.println("Proses transaksi dibatalkan.");
                return;
            }

            if (choice > 0 && choice <= pendingNotes.size()) {
                cNota notaToProcess = pendingNotes.get(choice - 1);
                System.out.println("\nMEMPROSES TRANSAKSI " + notaToProcess.getKode());
                notaToProcess.lihatNota();
                System.out.print("1. Proses Selesai | 2. Lewati (Kembali ke Menu Admin)\nPilihan: ");
                String aksi = sc.nextLine();

                int newStatus = -1;

                if (aksi.equals("1")) {
                    newStatus = 1;
                } else if (aksi.equals("2")) {
                    System.out.println("Transaksi dilewati. Kembali ke menu Admin.");
                    return;
                } else {
                    System.out.println("Pilihan tidak valid. Aksi dibatalkan.");
                    return;
                }

                if (newStatus != -1) {
                    try {
                        conn.setAutoCommit(false);
                        try (PreparedStatement ps = conn.prepareStatement("UPDATE transaksi SET status = ? WHERE kode_transaksi = ?")) {
                            ps.setInt(1, newStatus);
                            ps.setString(2, notaToProcess.getKode());
                            int rowsAffected = ps.executeUpdate();
                            if (rowsAffected > 0) {
                                notaToProcess.setStatus(newStatus);
                                if (newStatus == 1) {
                                    System.out.println("Transaksi " + notaToProcess.getKode() + " berhasil diproses (Selesai).");
                                    cPembeli p = notaToProcess.getPembeli();
                                    if (!p.getKodeMember().equals("Non-Member")) {
                                        try (PreparedStatement psUpdateMember = conn.prepareStatement("UPDATE pengguna SET total_belanja = total_belanja + ? WHERE id_pengguna = ?")) {
                                            psUpdateMember.setDouble(1, notaToProcess.hitungTotal());
                                            psUpdateMember.setString(2, p.getId());
                                            psUpdateMember.executeUpdate();
                                            p.tambahTotalBelanja(notaToProcess.hitungTotal());
                                        }
                                    }
                                }
                                conn.commit();
                                antreanTransaksi.remove(notaToProcess);
                            } else {
                                conn.rollback();
                                System.out.println("Gagal memperbarui status transaksi di database.");
                            }
                        }
                    } catch (SQLException e) {
                        System.err.println("Error saat memproses transaksi: " + e.getMessage());
                        try {
                            conn.rollback();
                        } catch (SQLException ex) { /* ignore */ }
                    } finally {
                        try {
                            conn.setAutoCommit(true);
                        } catch (SQLException ex) { /* ignore */ }
                    }
                }
            } else {
                System.out.println("Nomor transaksi tidak valid.");
            }
        }

        private static void tampilkanTransaksiBelumDiproses() {
            System.out.println("\n--- Tampilkan Transaksi Belum Diproses ---");
            boolean found = false;
            int count = 0;
            for (cNota nota : antreanTransaksi) {
                if (nota.getStatus() == 0) {
                    nota.lihatNota();
                    System.out.println("---------------------------------");
                    found = true;
                    count++;
                }
            }
            if (!found) {
                System.out.println("Tidak ada transaksi yang belum diproses saat ini.");
            } else {
                System.out.println("Total: " + count + " transaksi belum diproses.");
            }
        }
    }

    static class MenuOwner {
        public static void login() {
            System.out.print("Email Pemilik: ");
            String email = sc.nextLine();
            System.out.print("Password: ");
            String pw = sc.nextLine();
            try (PreparedStatement ps = conn.prepareStatement("SELECT id_pengguna, nama_pengguna, email, telepon FROM pengguna WHERE email = ? AND password = ? AND peran = 'pemilik'")) {
                ps.setString(1, email);
                ps.setString(2, pw);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    System.out.println("\nLogin berhasil sebagai " + rs.getString("nama_pengguna"));
                    menu();
                } else {
                    System.out.println("Login Gagal! Email atau Password salah.");
                }
            } catch (SQLException e) {
                System.err.println("Error saat login pemilik: " + e.getMessage());
            }
        }

        public static void menu() {
            boolean logout = false;
            while (!logout) {
                System.out.println("\n--- Menu Pemilik ---");
                System.out.println("1. Laporan Transaksi (Berdasarkan Status)"); // Poin 29
                System.out.println("2. Laporan Penjualan per Barang"); // Poin 32, 33
                System.out.println("3. Laporan Total Belanja Member"); // Poin 35, 36, 37
                System.out.println("4. Grafik Penjualan Barang"); // Poin 38, 39
                System.out.println("5. Ubah Harga Barang"); // Poin 31
                System.out.println("0. Logout"); // Poin 20d, exit dari akun Pemilik
                System.out.print("Pilihan: ");
                String pilihan = sc.nextLine();

                switch (pilihan) {
                    case "1":
                        JerukPerasPragita.laporanTransaksiBerdasarkanStatus();
                        break;
                    case "2":
                        JerukPerasPragita.laporanPenjualanPerBarang();
                        break;
                    case "3":
                        JerukPerasPragita.laporanKeanggotaanMember();
                        break;
                    case "4":
                        JerukPerasPragita.grafikPenjualan();
                        break;
                    case "5":
                        JerukPerasPragita.ubahHargaBarang(); 
                        break;
                    case "0":
                        logout = true;
                        System.out.println("Anda telah logout dari Pemilik.");
                        break;
                    default:
                        System.out.println("Pilihan tidak valid!");
                }
            }
        }
    }
    
    // =============================================================
    // METODE HELPER GLOBAL (static private)
    // Metode-metode ini dipindahkan ke level kelas JerukPerasPragita
    // agar bisa diakses oleh MenuAdmin dan MenuOwner dengan kualifikasi JerukPerasPragita.namaMetode()
    // =============================================================

    private static void laporanTransaksiBerdasarkanStatus() {
        System.out.println("\n--- Laporan Total Nilai Order ---");
        double totalSudahDiproses = 0;
        double totalBelumDiproses = 0;

        String sql = "SELECT total_akhir, status FROM transaksi";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                double totalAkhir = rs.getDouble("total_akhir");
                int status = rs.getInt("status");

                if (status == 1) { // 1 berarti sudah diproses
                    totalSudahDiproses += totalAkhir;
                } else if (status == 0) { // 0 berarti belum diproses
                    totalBelumDiproses += totalAkhir;
                }
            }
            System.out.printf("Total Sudah Diproses: Rp%,.0f\n", totalSudahDiproses);
            System.out.printf("Total Belum Diproses: Rp%,.0f\n", totalBelumDiproses);

        } catch (SQLException e) {
            System.err.println("Error saat membuat laporan berdasarkan status: " + e.getMessage());
        }
    }

    private static void laporanPenjualanPerBarang() {
        System.out.println("\n--- Laporan Penjualan per Barang ---");
        Map<String, Double> pendapatanPerBarang = new HashMap<>();
        double totalPendapatanKeseluruhan = 0;

        String sql = "SELECT b.nama_barang, SUM(dt.jumlah * dt.harga_satuan_saat_transaksi) AS total_item_penjualan " +
                     "FROM detail_transaksi dt " +
                     "JOIN barang b ON dt.id_barang = b.id_barang " +
                     "JOIN transaksi t ON dt.kode_transaksi = t.kode_transaksi " +
                     "WHERE t.status = 1 " + // Hanya hitung dari transaksi yang SUDAH DIPROSES/SELESAI
                     "GROUP BY b.nama_barang " +
                     "ORDER BY b.nama_barang";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (!rs.isBeforeFirst()) {
                System.out.println("Belum ada data penjualan yang sudah diproses per barang.");
                return;
            }

            while (rs.next()) {
                String namaBarang = rs.getString("nama_barang");
                double totalItemPenjualan = rs.getDouble("total_item_penjualan"); // Menggunakan alias kolom
                pendapatanPerBarang.put(namaBarang, totalItemPenjualan);
                totalPendapatanKeseluruhan += totalItemPenjualan;
            }

            System.out.printf("Total Pendapatan: Rp%,.0f\n", totalPendapatanKeseluruhan);
            int nomor = 1;
            for (Map.Entry<String, Double> entry : pendapatanPerBarang.entrySet()) {
                System.out.printf("%d. %-25s: Rp%,.0f\n", nomor++, entry.getKey(), entry.getValue());
            }
        } catch (SQLException e) {
            System.err.println("Error saat membuat laporan penjualan per barang: " + e.getMessage());
        }
    }

    private static void laporanKeanggotaanMember() {
        System.out.println("\n--- Laporan Total Belanja Member ---");
        loadMasterData(); // Pastikan data member terbaru dimuat, termasuk total_belanja

        if (daftarPembeli.isEmpty()) {
            System.out.println("Tidak ada member terdaftar.");
            return;
        }

        ArrayList<cPembeli> sortedPembeli = new ArrayList<>(daftarPembeli);
        sortedPembeli.sort((p1, p2) -> Double.compare(p2.getTotalBelanja(), p1.getTotalBelanja()));

        int no = 1;
        boolean foundMemberWithBelanja = false;
        for (cPembeli member : sortedPembeli) {
            if (!member.getKodeMember().equals("Non-Member") && member.getTotalBelanja() > 0) {
                System.out.printf("%d. %-15s : Rp%,.0f\n", no++, member.getNama(), member.getTotalBelanja());
                foundMemberWithBelanja = true;
            }
        }
        if (!foundMemberWithBelanja) {
            System.out.println("Belum ada member yang memiliki total belanja.");
        }
    }

    private static void grafikPenjualan() {
        System.out.println("\n--- GRAFIK PENJUALAN ---");
        System.out.println("(1 'X' = Rp 10.000, pembulatan ke bawah)");
        Map<String, Double> pendapatanGrafik = new HashMap<>();

        String sql = "SELECT b.nama_barang, SUM(dt.jumlah * dt.harga_satuan_saat_transaksi) AS total_penjualan_per_barang " +
                     "FROM detail_transaksi dt " +
                     "JOIN barang b ON dt.id_barang = b.id_barang " +
                     "JOIN transaksi t ON dt.kode_transaksi = t.kode_transaksi " +
                     "WHERE t.status = 1 " +
                     "GROUP BY b.nama_barang " +
                     "ORDER BY total_penjualan_per_barang DESC";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (!rs.isBeforeFirst()) {
                System.out.println("Belum ada data penjualan yang selesai untuk membuat grafik.");
                return;
            }

            int maxBarNameLength = 0;
            while (rs.next()) {
                String namaBarang = rs.getString("nama_barang");
                double totalJual = rs.getDouble("total_penjualan_per_barang");
                pendapatanGrafik.put(namaBarang, totalJual);
                if (namaBarang.length() > maxBarNameLength) {
                    maxBarNameLength = namaBarang.length();
                }
            }

            for (Map.Entry<String, Double> entry : pendapatanGrafik.entrySet()) {
                String namaBarang = entry.getKey();
                double totalJual = entry.getValue();
                int jumlahX = (int) (totalJual / 10000);
                String bar = "X".repeat(jumlahX);
                System.out.printf("%-" + (maxBarNameLength + 2) + "s: %s Rp%,.0f\n", namaBarang, bar, totalJual);
            }

        } catch (SQLException e) {
            System.err.println("Error saat membuat grafik penjualan: " + e.getMessage());
        }
    }

    // Metode ini akan diakses oleh Owner untuk mengubah harga barang
    private static void ubahHargaBarang() {
        lihatDaftarBarangGlobal(); // Dipanggil tanpa kualifikasi kelas karena berada di kelas yang sama
        if (daftarMinuman.isEmpty()) {
            System.out.println("Tidak ada barang untuk diubah.");
            return;
        }
        System.out.print("Masukkan ID Barang yang akan diubah harganya: ");
        int id;
        try {
            id = sc.nextInt();
            sc.nextLine();
        } catch (InputMismatchException e) {
            System.out.println("ID Barang harus angka!");
            sc.nextLine();
            return;
        }

        cMinuman minumanToUpdate = null;
        for (cMinuman m : daftarMinuman) {
            if (m.getId() == id) {
                minumanToUpdate = m;
                break;
            }
        }

        if (minumanToUpdate == null) {
            System.out.println("Barang dengan ID tersebut tidak ditemukan.");
            return;
        }

        System.out.println("Data Barang Saat Ini:");
        System.out.println("Nama: " + minumanToUpdate.getNama());
        System.out.println("Harga: " + minumanToUpdate.getHarga());

        System.out.print("Masukkan Harga Baru: Rp");
        int newHarga;
        try {
            newHarga = sc.nextInt();
            if (newHarga <= 0) {
                System.out.println("Harga harus lebih dari 0. Tidak ada perubahan harga.");
                sc.nextLine();
                return;
            }
        } catch (InputMismatchException e) {
            System.out.println("Input harga tidak valid. Tidak ada perubahan harga.");
            sc.nextLine();
            return;
        }
        sc.nextLine();

        try (PreparedStatement ps = conn.prepareStatement("UPDATE barang SET harga = ? WHERE id_barang = ?")) {
            ps.setInt(1, newHarga);
            ps.setInt(2, id);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Harga barang berhasil diubah.");
                loadMasterData();
            } else {
                System.out.println("Gagal mengubah harga barang.");
            }
        } catch (SQLException e) {
            System.err.println("Error saat mengubah harga barang: " + e.getMessage());
        }
    }
    
    // Metode ini dulu ada di MenuAdmin, sekarang di level kelas utama
    // Dipertahankan sebagai helper global, meskipun tidak dipanggil dari MenuAdmin saat ini.
    private static void tambahBarang() {
        System.out.print("Nama Barang: ");
        String nama = sc.nextLine();
        System.out.print("Harga: ");
        int harga;
        try {
            harga = sc.nextInt();
            if (harga <= 0) {
                System.out.println("Harga harus lebih dari 0.");
                sc.nextLine();
                return;
            }
        } catch (InputMismatchException e) {
            System.out.println("Harga harus angka!");
            sc.nextLine();
            return;
        }
        System.out.print("Stok: ");
        int stok;
        try {
            stok = sc.nextInt();
            if (stok < 0) {
                System.out.println("Stok tidak boleh negatif.");
                sc.nextLine();
                return;
            }
        } catch (InputMismatchException e) {
            System.out.println("Stok harus angka!");
            sc.nextLine();
            return;
        }
        sc.nextLine();

        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO barang (nama_barang, harga, stok) VALUES (?, ?, ?)")) {
            ps.setString(1, nama);
            ps.setInt(2, harga);
            ps.setInt(3, stok);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Barang berhasil ditambahkan.");
                loadMasterData();
            } else {
                System.out.println("Gagal menambahkan barang.");
            }
        } catch (SQLException e) {
            System.err.println("Error saat menambah barang: " + e.getMessage());
        }
    }
    
    // Metode ini dulu ada di MenuAdmin, sekarang di level kelas utama
    // Dipertahankan sebagai helper global, meskipun tidak dipanggil dari MenuAdmin saat ini.
    private static void hapusBarang() {
        lihatDaftarBarangGlobal();
        if (daftarMinuman.isEmpty()) {
            System.out.println("Tidak ada barang untuk dihapus.");
            return;
        }
        System.out.print("Masukkan ID Barang yang akan dihapus: ");
        int id;
        try {
            id = sc.nextInt();
            sc.nextLine();
        } catch (InputMismatchException e) {
            System.out.println("ID Barang harus angka!");
            sc.nextLine();
            return;
        }

        System.out.print("Apakah Anda yakin ingin menghapus barang dengan ID " + id + "? (y/n): ");
        String konfirmasi = sc.nextLine();
        if (!konfirmasi.equalsIgnoreCase("y")) {
            System.out.println("Penghapusan barang dibatalkan.");
            return;
        }

        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM barang WHERE id_barang = ?")) {
            ps.setInt(1, id);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Barang berhasil dihapus.");
                loadMasterData();
            } else {
                System.out.println("Barang dengan ID tersebut tidak ditemukan.");
            }
        } catch (SQLException e) {
            System.err.println("Error saat menghapus barang: " + e.getMessage());
            if (e.getErrorCode() == 1451) {
                System.err.println("Tidak dapat menghapus barang karena ada transaksi yang terkait.");
            }
        }
    }

    // Metode ini digunakan oleh Admin (jika ada menu kelola barang) dan Owner (laporan stok/ubah harga)
    // Dibuat static dan di level global JerukPerasPragita
    private static void lihatDaftarBarangGlobal() {
        loadMasterData();
        if (daftarMinuman.isEmpty()) {
            System.out.println("Daftar barang kosong.");
            return;
        }
        System.out.println("\n--- Daftar Barang Tersedia ---");
        System.out.printf("%-5s %-25s %-15s %-10s\n", "ID", "Nama Barang", "Harga", "Stok");
        System.out.println("----------------------------------------------------------");
        for (cMinuman m : daftarMinuman) {
            System.out.printf("%-5d %-25s Rp%,-12d %-10d\n", m.getId(), m.getNama(), m.getHarga(), m.getStok());
        }
        System.out.println("----------------------------------------------------------");
    }
}