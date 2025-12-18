package ipca.example.musicastock.domain.models


data class Music(
    val musId: String = "",

    val musTitle: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val releaseDate: String? = null,
    val audioUrl: String? = null,
    // extras locais
    val musStyle: String? = null,
    val tabUrl: String? = null,
    // pode ser null (música fora de coletânea)
    val collectionId: String? = null
)
