package com.jordan.controller;

import com.jordan.model.Pessoa;
import com.jordan.model.Telefone;
import com.jordan.repository.PessoaRepository;
import com.jordan.repository.TelefoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;

@Controller
public class PessoaController {

    @Autowired
    private PessoaRepository pessoaRepository;
    @Autowired
    private TelefoneRepository telefoneRepository;

    @RequestMapping(method = RequestMethod.GET, value = "/cadastropessoa")
    public ModelAndView inicio() {

        ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
        Iterable<Pessoa> pessoaIterable = pessoaRepository.findAll();
        andView.addObject("pessoas", pessoaIterable);
        andView.addObject("pessoaobj", new Pessoa());
        return andView;
    }

    @RequestMapping(method = RequestMethod.POST, value = "**/salvarpessoa")
    public ModelAndView salvar(Pessoa pessoa) {
        pessoaRepository.save(pessoa);

        ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
        Iterable<Pessoa> pessoaIterable = pessoaRepository.findAll();
        andView.addObject("pessoas", pessoaIterable);

        andView.addObject("pessoaobj", new Pessoa());

        return andView;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/listapessoas")
    public ModelAndView pessoas() {

        ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
        Iterable<Pessoa> pessoaIterable = pessoaRepository.findAll();
        andView.addObject("pessoas", pessoaIterable);
        andView.addObject("pessoaobj", new Pessoa());

        return andView;
    }

    @GetMapping("/editarpessoa/{idpessoa}")
    public ModelAndView editar(@PathVariable("idpessoa") Long idpessoa) {

        ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
        Optional<Pessoa> pessoa = pessoaRepository.findById(idpessoa);
        andView.addObject("pessoaobj", pessoa.get());

        return andView;
    }
    @GetMapping("/removerpessoa/{idpessoa}")
    public ModelAndView excluir(@PathVariable("idpessoa") Long idpessoa) {

        pessoaRepository.deleteById(idpessoa);

        ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
        andView.addObject("pessoas", pessoaRepository.findAll());
        andView.addObject("pessoaobj", new Pessoa());

        return andView;
    }

    @PostMapping("**/pesquisarpessoa")
    public ModelAndView pesquisar(@RequestParam("nomepesquisa") String nomepesquisa) {

        ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
        andView.addObject("pessoas", pessoaRepository.findPessoaByName(nomepesquisa));
        andView.addObject("pessoaobj", new Pessoa());

        return andView;
    }

    @GetMapping("/telefones/{idpessoa}")
    public ModelAndView telefones(@PathVariable("idpessoa") Long idpessoa) {

        ModelAndView andView = new ModelAndView("cadastro/telefones");
        Optional<Pessoa> pessoa = pessoaRepository.findById(idpessoa);
        andView.addObject("pessoaobj", pessoa.get());


        return andView;
    }

    @PostMapping("**/addfonepessoa/{pessoaid}")
    public ModelAndView addFonePessoa(Telefone telefone, @PathVariable("pessoaid") Long pessoaid) {

        Pessoa pessoa = pessoaRepository.findById(pessoaid).get();
        telefone.setPessoa(pessoa);

        telefoneRepository.save(telefone);

        ModelAndView andView = new ModelAndView("cadastro/telefones");
        andView.addObject("pessoaobj", pessoa);

        return andView;
    }
}
