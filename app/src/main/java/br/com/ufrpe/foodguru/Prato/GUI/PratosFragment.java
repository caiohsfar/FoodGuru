package br.com.ufrpe.foodguru.Prato.GUI;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import br.com.ufrpe.foodguru.R;
import br.com.ufrpe.foodguru.Prato.dominio.Prato;
import br.com.ufrpe.foodguru.Prato.dominio.PratoHolder;
import br.com.ufrpe.foodguru.Prato.dominio.PratoView;
import br.com.ufrpe.foodguru.Prato.negocio.PratoServices;
import br.com.ufrpe.foodguru.Prato.dominio.SessaoCardapio;
import br.com.ufrpe.foodguru.infraestrutura.persistencia.FirebaseHelper;

import static br.com.ufrpe.foodguru.infraestrutura.persistencia.FirebaseHelper.REFERENCIA_PRATO;

/**
 * A simple {@link Fragment} subclass.
 */
public class PratosFragment extends Fragment{
    private PratoAdapter adapter;
    private RecyclerView mRecyclerView;
    private PratoServices pratoServices = new PratoServices();
    private List<PratoView> pratosViews;
    private ActionMode actionMode;
    private View viewInflado;
    private Spinner sessao;
    private List<SessaoCardapio> arraySessoes;
    private ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewInflado = inflater.inflate(R.layout.fragment_pratos, container, false);
        progressBar = viewInflado.findViewById(R.id.progress_bar_pratos);
        sessao = (Spinner) viewInflado.findViewById(R.id.spinnerSessao);
        loadArraySessoes();
        iniciarRecyclerView();
        setCliqueAdapterSessoes();

        return viewInflado;
    }

    private  ActionMode.Callback getActionModeCallback(){
        return new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.action_mode, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                menu.findItem(R.id.acao_editar).setVisible(false);
                return true;
            }

            @Override
            //quando aparece o menu e as opções de menu exibidas para serem clicadas
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()){
                    case R.id.selecionar_todos:{
                        selecionarTodos();
                        break;
                    }
                    case R.id.deletar:{
                        exibirAlertDialog();
                        break;
                    }
                    case R.id.acao_editar:{
                        abrirTelaEditarPrato();
                        actionMode.finish();
                        break;
                    }
                }

                return true;
            }


            @Override
            public void onDestroyActionMode(ActionMode mode) {
                actionMode = null;
                for (PratoView prato : pratosViews) {
                    prato.setSelecionado(false);
                }
                mRecyclerView.getAdapter().notifyDataSetChanged();

            }
        };
    }

    private void deletarPratos(){
        for (PratoView pratoView : pratosViews){
            if (pratoView.isSelecionado()){
                pratoServices.removerPrato(pratoView.getPrato());
            }
        }
    }

    private void selecionarTodos(){
        for(PratoView pratoView : pratosViews){
            if (!pratoView.isSelecionado()){
                pratoView.setSelecionado(true);
                updateActionModeTitle();
                updateIconeEditar();
                mRecyclerView.getAdapter().notifyDataSetChanged();
            }
        }
    }

    public PratosFragment() {
        // Required empty public constructor
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
                    loadPratosBySessao(idSessao);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
        sessao.setOnItemSelectedListener(escolha);

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void loadPratosBySessao(String idSesao){
        FirebaseHelper.getFirebaseReference().child(FirebaseHelper.REFERENCIA_ESTABELECIMENTO)
                .child(FirebaseHelper.getFirebaseAuth().getCurrentUser().getUid())
                .child(REFERENCIA_PRATO).orderByChild("idSessao")
                .equalTo(idSesao)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        pratosViews = pratoToPratoView((ArrayList<Prato>)pratoServices.loadPratos(dataSnapshot));
                        adapter = new PratoAdapter(getContext(),pratosViews,onClickPrato());
                        mRecyclerView.setAdapter(adapter);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    public void loadTodosPratos(){
        FirebaseHelper.getFirebaseReference().child(FirebaseHelper.REFERENCIA_ESTABELECIMENTO)
                .child(FirebaseHelper.getFirebaseAuth().getCurrentUser().getUid())
                .child(REFERENCIA_PRATO).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.VISIBLE);
                pratosViews = pratoToPratoView((ArrayList<Prato>)pratoServices.loadPratos(dataSnapshot));
                adapter = new PratoAdapter(getContext(),pratosViews,onClickPrato());
                mRecyclerView.setAdapter(adapter);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    public void loadArraySessoes(){
        FirebaseHelper.getFirebaseReference().child(FirebaseHelper.REFERENCIA_ESTABELECIMENTO)
                .child(FirebaseHelper.getUidUsuario())
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

    private void setupSpinner() {

        ArrayAdapter<SessaoCardapio> adapterSessao = new ArrayAdapter<SessaoCardapio>(viewInflado.getContext(),R.layout.spinner_edit_sessao,arraySessoes);
        adapterSessao.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sessao.setAdapter(adapterSessao);
    }

    private void iniciarActionMode(int indexPrato) {
        actionMode = getActivity().startActionMode(getActionModeCallback());
        PratoView prato = pratosViews.get(indexPrato);
        prato.setSelecionado(true);
        mRecyclerView.getAdapter().notifyDataSetChanged();
        updateActionModeTitle();
        updateIconeEditar();
    }

    private PratoAdapter.PratoOnClickListener onClickPrato() {
        return new PratoAdapter.PratoOnClickListener() {
            @Override
            public void onClickPrato(PratoHolder holder, int indexPrato) {
                Prato prato = pratosViews.get(indexPrato).getPrato();
                if (actionMode == null) {
                    //detalharPrato(prato);
                } else if (!pratosViews.get(indexPrato).isSelecionado()){
                    selecionarItem(indexPrato);
                }else{
                    desSelecionarItem(indexPrato);
                }
            }
            @Override
            public void onLongClickPrato(PratoHolder holder, int indexPrato) {
                if (actionMode != null) {
                    return;
                }
                iniciarActionMode(indexPrato);
            }

        };
    }

    /*
    private void detalharPrato(Prato prato) {
        Intent intent = new Intent(getContext(),DetalhesPratoActivity.class);
        intent.putExtra("NOME_PRATO",prato.getNomePrato());
        intent.putExtra("DESCRICAO_PRATO", prato.getDescricaoPrato());
        startActivity(intent);
    }
    */

    private void desSelecionarItem(int indexPrato) {
        pratosViews.get(indexPrato).setSelecionado(false);
        mRecyclerView.getAdapter().notifyDataSetChanged();
        if (pratosViews.size() == 0){
            actionMode.finish();
        }else{
            updateIconeEditar();
            updateActionModeTitle();
            mRecyclerView.getAdapter().notifyDataSetChanged();
        }

    }

    private void selecionarItem(int indexPrato) {
        pratosViews.get(indexPrato).setSelecionado(true);
        mRecyclerView.getAdapter().notifyDataSetChanged();
        updateIconeEditar();
        updateActionModeTitle();
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }
    private void updateIconeEditar(){
        List<PratoView> pratosSelecionadas = getPratosSelecionados();
        if (pratosSelecionadas.size() == 1){
            actionMode.getMenu().findItem(R.id.acao_editar).setEnabled(true);
            actionMode.getMenu().findItem(R.id.acao_editar).setVisible(true);
        }else if (pratosSelecionadas.size() > 1){
            actionMode.getMenu().findItem(R.id.acao_editar).setEnabled(false);
            actionMode.getMenu().findItem(R.id.acao_editar).setVisible(false);
        }
    }

    private void updateActionModeTitle() {
        List<PratoView> pratosSelecionados = getPratosSelecionados();
        if (actionMode != null) {
            if (pratosSelecionados.size() == 0){
                actionMode.finish();
            }else{
                actionMode.setTitle(String.valueOf(pratosSelecionados.size()));
            }
        }
    }
    private List<PratoView> getPratosSelecionados() {
        List<PratoView> list = new ArrayList<PratoView>();
        for (PratoView prato : this.pratosViews) {
            if (prato.isSelecionado()) {
                list.add(prato);
            }
        }
        return list;
    }

    private List<PratoView> pratoToPratoView(ArrayList<Prato> pratos){
        ArrayList<PratoView> pratosViews = new ArrayList<>();
        for (Prato prato : pratos) {
            PratoView pratoView = new PratoView();
            pratoView.setPrato(prato);
            pratosViews.add(pratoView);
        }
        return pratosViews;
    }


    public void iniciarRecyclerView(){
        mRecyclerView = (RecyclerView) viewInflado.findViewById(R.id.recyclerv_view_pratos);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext()
                , 1);
        //LinearLayoutManager layoutManager = new LinearLayoutManager(viewInflado.getContext(), LinearLayoutManager.VERTICAL, false);
        //mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutManager);
        adapter = new PratoAdapter(getContext(),pratosViews, onClickPrato());
        mRecyclerView.setAdapter(adapter);


    }

    public void abrirTelaEditarPrato(){
        Prato pratoSelecionado = getPratoSelecionado();
        Intent intent = new Intent(viewInflado.getContext(),EditarPratoActivity.class);
        intent.putExtra("prato", pratoSelecionado);
        startActivity(intent);
    }
    public Prato getPratoSelecionado() {
        for (PratoView pratoView : pratosViews) {
            if (pratoView.isSelecionado()) {
                return pratoView.getPrato();
            }
        }
        return null;
    }

    public void exibirAlertDialog(){
        AlertDialog.Builder builderDeletar = setarMensagemDeletar();
        setarBotaoNegativoDeletar(builderDeletar);
        setarBotaoPositivoDeletar(builderDeletar);
        exibirTextoConfirmarDelecao(builderDeletar);

    }
    public AlertDialog.Builder setarMensagemDeletar(){
        AlertDialog.Builder  builder = new AlertDialog.Builder(
                getContext());
        builder.setMessage("Você deseja mesmo deletar os pratos?");
        return builder;
    }

    public void setarBotaoNegativoDeletar(AlertDialog.Builder builderDeletar){
        builderDeletar.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
    }
    public void exibirTextoConfirmarDelecao(AlertDialog.Builder builderDeletar){
        AlertDialog alert =  builderDeletar.create();
        alert.setTitle("Confirmação");
        alert.show();
    }
    public void setarBotaoPositivoDeletar(AlertDialog.Builder builderDeletar){
        builderDeletar.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                deletarPratos();
                actionMode.finish();
            }
        });
    }
    public void abrirTelaSessao() {
        Intent intent = new Intent(getContext(), SessaoActvity.class);
        startActivity(intent);
    }
}
