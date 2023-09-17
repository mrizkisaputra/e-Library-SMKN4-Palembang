package sch.id.smkn4palembang.datastore

import sch.id.smkn4palembang.R
import sch.id.smkn4palembang.model.EbookMenu

object DataSource {
    val getEbookMenu: List<EbookMenu>
        get() {
            return listOf(
                EbookMenu(R.drawable.books, "Buku Paket Sekolah", "https://annibuku.com/bse/jenjang-pendidikan/sma-smk-kelas-10"),
                EbookMenu(R.drawable.books, "Kurikulum Merdeka", "https://buku.kemdikbud.go.id/katalog/buku-kurikulum-merdeka"),
                EbookMenu(R.drawable.books, "Kurikulum K13", "https://buku.kemdikbud.go.id/katalog/buku-teks-k13"),
                EbookMenu(R.drawable.books, "Buku Madrasah", "https://fliphtml5.com/bookcase/byuqs"),
                EbookMenu(R.drawable.books, "Fiqih Islam", "https://www.rumahfiqih.com/pdf/"),
                EbookMenu(R.drawable.books, "Literasi Digital", "https://literasidigital.id/buku"),
                EbookMenu(R.drawable.books, "Gerakan Literasi Sekolah", "https://repositori.kemdikbud.go.id/4809/1/Buku%20Gerakan%20Literasi%20Sekolah.pdf"),
                EbookMenu(R.drawable.books, "Gerakan Literasi Masyarakat", "https://repositori.kemdikbud.go.id/4809/1/Buku%20Gerakan%20Literasi%20Sekolah.pdf"),
                EbookMenu(R.drawable.books, "Gerakan Literasi Keluarga", "https://repositori.kemdikbud.go.id/4809/1/Buku%20Gerakan%20Literasi%20Sekolah.pdf"),
                EbookMenu(R.drawable.books, "Non Teks / Bergambar", "https://buku.kemdikbud.go.id/katalog/buku-non-teks"),
            )
        }


}