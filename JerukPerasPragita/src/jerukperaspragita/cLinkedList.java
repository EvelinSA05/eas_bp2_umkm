package jerukperaspragita;

public class cLinkedList {
    cSimpulTransaksi head, tail; // Kita pakai nama baru untuk node transaksi
    int jumlah;

    public cLinkedList() {
        head = tail = null;
        jumlah = 0;
    }
    
    // Metode untuk menambahkan nota ke antrean
    public void enqueue(cNota notaBaru) {
        cSimpulTransaksi baru = new cSimpulTransaksi(notaBaru);
        if (head == null) {
            head = tail = baru;
        } else {
            tail.next = baru;
            tail = baru;
        }
        jumlah++;
        System.out.println("Transaksi ["+notaBaru.getKode()+"] berhasil ditambahkan ke antrean.");
    }
    
    public cSimpulTransaksi getHead() {
        return head;
    }

    public int getJumlah() { // Getter untuk jumlah transaksi di antrean
        return jumlah;
    }

    public boolean isEmpty() { // Cek apakah antrean kosong
        return head == null;
    }

    // Metode untuk mengambil nota berdasarkan indeks (tidak ideal untuk queue, tapi jika dibutuhkan)
    public cNota get(int index) {
        if (index < 0 || index >= jumlah) {
            return null; // Atau lempar IndexOutOfBoundsException
        }
        cSimpulTransaksi current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current.data;
    }
    
    public cNota dequeue() { // Ambil dan hapus dari depan (FIFO)
        if (head == null) {
            return null;
        }
        cNota data = head.data;
        head = head.next;
        if (head == null) { // Jika setelah dequeue, list jadi kosong
            tail = null;
        }
        jumlah--;
        return data;
    }

    // Metode untuk menghapus node berdasarkan objek cNota yang diberikan
    public boolean remove(cNota targetNota) {
        if (head == null) {
            return false;
        }

        if (head.data.equals(targetNota)) { // Jika target adalah head
            head = head.next;
            if (head == null) {
                tail = null;
            }
            jumlah--;
            return true;
        }

        cSimpulTransaksi current = head;
        while (current.next != null && !current.next.data.equals(targetNota)) {
            current = current.next;
        }

        if (current.next != null) { // Jika target ditemukan
            if (current.next == tail) { // Jika target adalah tail
                tail = current;
            }
            current.next = current.next.next;
            jumlah--;
            return true;
        }
        return false; // Target tidak ditemukan
    }
}