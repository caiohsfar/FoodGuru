package br.com.ufrpe.foodguru.Prato.dominio;

public class SessaoCardapio {
    public String id;
    public String nome;
    public String idEstabelecimento;

    public String getIdEstabelecimento() { return idEstabelecimento; }

    public void setIdEstabelecimento(String idEstabelecimento) { this.idEstabelecimento = idEstabelecimento; }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @Override
    public String toString() {
        return getNome();
    }
}