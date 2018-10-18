package br.com.ufrpe.foodguru.cliente.GUI;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import br.com.ufrpe.foodguru.Mesa.dominio.Mesa;
import br.com.ufrpe.foodguru.Prato.GUI.DetalhesPratoClienteActvity;
import br.com.ufrpe.foodguru.Prato.GUI.PratoAdapter;
import br.com.ufrpe.foodguru.Prato.dominio.Prato;
import br.com.ufrpe.foodguru.Prato.dominio.PratoView;
import br.com.ufrpe.foodguru.Prato.dominio.SessaoCardapio;
import br.com.ufrpe.foodguru.Prato.negocio.PratoServices;
import br.com.ufrpe.foodguru.R;
import br.com.ufrpe.foodguru.infraestrutura.persistencia.FirebaseHelper;

import static br.com.ufrpe.foodguru.infraestrutura.persistencia.FirebaseHelper.REFERENCIA_PRATO;

/**
 * A simple {@link Fragment} subclass.
 */
public class CardapioFragment extends Fragment implements CardapioAdapter.OnItemClicked {
    private CardapioAdapter adapter;
    private RecyclerView mRecyclerView;
    private PratoServices pratoServices = new PratoServices();
    private List<Prato> pratosViews;
    private Spinner sessao;
    private List<SessaoCardapio> arraySessoes;
    private Mesa mesa;
    private View viewInflado;


    public CardapioFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        viewInflado = inflater.inflate(R.layout.fragment_cardapio, container, false);
        mesa = getActivity().getIntent().getExtras().getParcelable("mesa");
        sessao = (Spinner) viewInflado.findViewById(R.id.spinnerSessaoCardapio);

        loadArraySessoes();
        iniciarRecyclerView();
        setCliqueAdapterSessoes();


       return viewInflado;
    }

    public void setCliqueAdapterSessoes(){
        AdapterView.OnItemSelectedListener escolha = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int posicao =  sessao.getSelectedItemPosition();
                if (posicao == 0){
                    loadTodosPratos();
                }else{
                    String idSessao = arraySessoes.get(posicao).getId();
                    loadCardapioBySessao(idSessao);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
        sessao.setOnItemSelectedListener(escolha);

    }

    public void loadArraySessoes(){
        FirebaseHelper.getFirebaseReference().child(FirebaseHelper.REFERENCIA_ESTABELECIMENTO)
                .child(mesa.getUidEstabelecimento())
                .child(FirebaseHelper.REFERENCIA_SESSAO)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        PratoServices pratoServices = new PratoServices();
                        arraySessoes = pratoServices.loadSessoes(dataSnapshot);
                        arraySessoes.add(0, new SessaoCardapio("Todos os pratos"));
                        setupSpinner();
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
    public void loadTodosPratos(){
        FirebaseHelper.getFirebaseReference().child(FirebaseHelper.REFERENCIA_ESTABELECIMENTO)
                .child(mesa.getUidEstabelecimento())
                .child(REFERENCIA_PRATO).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                pratosViews = pratoServices.loadPratos(dataSnapshot);
                adapter = new CardapioAdapter(getContext(), pratosViews);
                mRecyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    public void loadCardapioBySessao(String idSesao){
        FirebaseHelper.getFirebaseReference().child(FirebaseHelper.REFERENCIA_ESTABELECIMENTO)
                .child(mesa.getUidEstabelecimento())
                .child(REFERENCIA_PRATO).orderByChild("idSessao")
                .equalTo(idSesao)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        pratosViews =pratoServices.loadPratos(dataSnapshot);
                        adapter = new CardapioAdapter(getContext(),pratosViews);
                        mRecyclerView.setAdapter(adapter);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
    private void setupSpinner() {
        ArrayAdapter<SessaoCardapio> adapterSessao = new ArrayAdapter<SessaoCardapio>(getContext(), android.R.layout.simple_spinner_dropdown_item,arraySessoes);
        adapterSessao.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sessao.setAdapter(adapterSessao);
    }

    public void iniciarRecyclerView() {
        mRecyclerView = (RecyclerView) viewInflado.findViewById(R.id.recycler_view_cardápio);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext()
                , LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutManager);
        adapter = new CardapioAdapter(getContext(), pratosViews);


    }


    @Override
    public void onItemClick(int position) {
        Intent abrirTelaDetalhes = new Intent(getContext(),DetalhesPratoClienteActvity.class);
        abrirTelaDetalhes.putExtra("imagem",adapter.getItem(position).getUrlImagem());
        abrirTelaDetalhes.putExtra("nome",adapter.getItem(position).getNomePrato());
        abrirTelaDetalhes.putExtra("descricao",adapter.getItem(position).getDescricaoPrato());
        startActivity(abrirTelaDetalhes);

    }
}
