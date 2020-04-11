package com.jordan.controller;

import com.jordan.model.Pessoa;
import com.jordan.model.Telefone;
import com.jordan.repository.PessoaRepository;
import com.jordan.repository.ProfissaoRepository;
import com.jordan.repository.TelefoneRepository;
import com.jordan.utils.ReportUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class PessoaController {

    @Autowired
    private PessoaRepository pessoaRepository;
    @Autowired
    private TelefoneRepository telefoneRepository;
    @Autowired
    private ReportUtil<Pessoa> reportUtil;
    @Autowired
    private ProfissaoRepository profissaoRepository;

    @GetMapping("/cadastropessoa")
    public ModelAndView inicio() {

        ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
        andView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));
        andView.addObject("pessoaobj", new Pessoa());
        andView.addObject("profissoes", profissaoRepository.findAll());

        return andView;
    }

    @GetMapping("/pessoaspag")
    public ModelAndView carregaPessoaPorPaginacao(@PageableDefault(size = 5) Pageable pageable, ModelAndView model,
                                                  @RequestParam("nomepesquisa")String nomepesquisa) {

        Page<Pessoa> pagePessoa = pessoaRepository.findPessoaByNamePage(nomepesquisa, pageable);
        model.addObject("pessoas", pagePessoa);
        model.addObject("pessoaobj", new Pessoa());
        model.addObject("nomepesquisa", nomepesquisa);
        model.setViewName("cadastro/cadastropessoa");

        return model;
    }

    @RequestMapping(method = RequestMethod.POST, value = "**/salvarpessoa", consumes = {"multipart/form-data"})
    public ModelAndView salvar(@Valid Pessoa pessoa,
                               BindingResult bindingResult, final MultipartFile file) throws IOException {

        pessoa.setTelefones(telefoneRepository.getTelefones(pessoa.getId()));

        if (bindingResult.hasErrors()) {
            ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");

            andView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));
            andView.addObject("pessoaobj", pessoa);
            andView.addObject("profissoes", profissaoRepository.findAll());

            List<String> msg = new ArrayList<>();
            for (ObjectError objectError : bindingResult.getAllErrors()) {
                msg.add(objectError.getDefaultMessage()); // msg padrão das anotações NotNull e NotEmpty
            }
            andView.addObject("msg", msg);

            return andView;
        } else {

            if (file.getSize() > 0) {

                pessoa.setCurriculo(file.getBytes());
                pessoa.setNomeFileCurriculo(file.getOriginalFilename());
                pessoa.setTipoFileCurriculo(file.getContentType());

            } else if (pessoa.getId() != null && pessoa.getId() > 0) { /* editando */

                Pessoa pessoaTemp = pessoaRepository.findById(pessoa.getId()).get();
                pessoa.setCurriculo(pessoaTemp.getCurriculo());
                pessoa.setNomeFileCurriculo(pessoaTemp.getNomeFileCurriculo());
                pessoa.setTipoFileCurriculo(pessoaTemp.getTipoFileCurriculo());
            }

            pessoaRepository.save(pessoa);

            ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");

            andView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));
            andView.addObject("pessoaobj", new Pessoa());
            andView.addObject("profissoes", profissaoRepository.findAll());

            return andView;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/listapessoas")
    public ModelAndView pessoas() {

        ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
        andView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));
        andView.addObject("pessoaobj", new Pessoa());
        andView.addObject("profissoes", profissaoRepository.findAll());

        return andView;
    }

    @GetMapping("/editarpessoa/{idpessoa}")
    public ModelAndView editar(@PathVariable("idpessoa") Long idpessoa) {

        ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
        Optional<Pessoa> pessoa = pessoaRepository.findById(idpessoa);
        andView.addObject("pessoaobj", pessoa.get());
        andView.addObject("profissoes", profissaoRepository.findAll());

        return andView;
    }

    @GetMapping("/removerpessoa/{idpessoa}")
    public ModelAndView excluir(@PathVariable("idpessoa") Long idpessoa) {

        pessoaRepository.deleteById(idpessoa);

        ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
        andView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));
        andView.addObject("pessoaobj", new Pessoa());
        andView.addObject("profissoes", profissaoRepository.findAll());

        return andView;
    }

    @PostMapping("**/pesquisarpessoa")
    public ModelAndView pesquisar(@RequestParam("nomepesquisa") String nomepesquisa,
                                  @RequestParam("sexopesquisa") String sexopesquisa,
                                  @PageableDefault(size = 5, sort = {"nome"}) Pageable pageable) {

        Page<Pessoa> pessoas = null;

        if (sexopesquisa != null && !sexopesquisa.isEmpty()) {
            //pessoas = pessoaRepository.findPessoaBySexoPage(nomepesquisa, sexopesquisa, pageable);
            pessoas = pessoaRepository.findPessoaBySexoPage(nomepesquisa, sexopesquisa, pageable);
        } else {
            pessoas = pessoaRepository.findPessoaByNamePage(nomepesquisa, pageable);
        }


        ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
        andView.addObject("pessoas", pessoas);
        andView.addObject("pessoaobj", new Pessoa());
        andView.addObject("nomepesquisa", nomepesquisa);
        andView.addObject("profissoes", profissaoRepository.findAll());

        return andView;
    }

    @GetMapping("**/pesquisarpessoa")
    public void imprimePDF(@RequestParam("nomepesquisa") String nomepesquisa,
                           @RequestParam("sexopesquisa") String sexopesquisa,
                           HttpServletRequest request,
                           HttpServletResponse response) throws Exception {

        List<Pessoa> pessoas = new ArrayList<Pessoa>();

        if (sexopesquisa != null && !sexopesquisa.isEmpty() && nomepesquisa != null && !nomepesquisa.isEmpty()) { /* Busca por nome e sexo */

            pessoas = pessoaRepository.findPessoaByNameSexo(nomepesquisa, sexopesquisa);

        } else if (nomepesquisa != null && !nomepesquisa.isEmpty()) { /* Busca somente por nome */

            pessoas = pessoaRepository.findPessoaByName(nomepesquisa);

        } else if (sexopesquisa != null && !sexopesquisa.isEmpty()) { /* Busca somente por sexo */

            pessoas = pessoaRepository.findPessoaBySexo(sexopesquisa);

        } else { /* Busca todos */

            Iterable<Pessoa> iterator = pessoaRepository.findAll();
            for (Pessoa pessoa : iterator) {
                pessoas.add(pessoa);
            }
        }

        /* Chama o serviço que faz a geração do relatório */
        byte[] pdf = reportUtil.gerarRelatorio(pessoas, "pessoa", request.getServletContext());

        /* Tamanho da resposta */
        response.setContentLength(pdf.length);

        /* Definir resposta o tipo do arquivo */
        response.setContentType("application/octet-stream");

        /* Definir o cabeçalho da resposta */
        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"", "relatorio.pdf");
        response.setHeader(headerKey, headerValue);

        /* Finaliza a resposta pro navegador */
        response.getOutputStream().write(pdf);
    }

    @GetMapping("/telefones/{idpessoa}")
    public ModelAndView telefones(@PathVariable("idpessoa") Long idpessoa) {

        ModelAndView andView = new ModelAndView("cadastro/telefones");
        Optional<Pessoa> pessoa = pessoaRepository.findById(idpessoa);
        andView.addObject("pessoaobj", pessoa.get());
        andView.addObject("telefones", telefoneRepository.getTelefones(idpessoa));

        return andView;
    }

    @PostMapping("**/addfonepessoa/{pessoaid}")
    public ModelAndView addFonePessoa(Telefone telefone, @PathVariable("pessoaid") Long pessoaid) {

        Pessoa pessoa = pessoaRepository.findById(pessoaid).get();

        if (telefone != null && telefone.getNumero().isEmpty() || telefone.getTipo().isEmpty()) {
            ModelAndView andView = new ModelAndView("cadastro/telefones");
            andView.addObject("pessoaobj", pessoa);
            andView.addObject("telefones", telefoneRepository.getTelefones(pessoaid));

            List<String> msg = new ArrayList<>();
            andView.addObject("msg", msg);
            if (telefone.getNumero().isEmpty()) {
                msg.add("Número deve ser informado");
            }
            if (telefone.getTipo().isEmpty()) {
                msg.add("Tipo deve ser informado");
            }
            return andView;
        } else {

            telefone.setPessoa(pessoa);

            telefoneRepository.save(telefone);


            ModelAndView andView = new ModelAndView("cadastro/telefones");
            andView.addObject("pessoaobj", pessoa);
            andView.addObject("telefones", telefoneRepository.getTelefones(pessoaid));

            return andView;
        }
    }

    @GetMapping("/removertelefone/{idtelefone}")
    public ModelAndView excluirTelefone(@PathVariable("idtelefone") Long idtelefone) {

        Pessoa pessoa = telefoneRepository.findById(idtelefone).get().getPessoa();

        telefoneRepository.deleteById(idtelefone);

        ModelAndView andView = new ModelAndView("cadastro/telefones");
        andView.addObject("pessoaobj", pessoa);
        andView.addObject("telefones", telefoneRepository.getTelefones(pessoa.getId()));

        return andView;
    }

    @GetMapping("**/baixarcurriculo/{idpessoa}")
    public void baixarcurriculo(@PathVariable("idpessoa") Long idpessoa, HttpServletResponse response) throws IOException {

        /* Consulta o objeto pessoa no banco de dados */
        Pessoa pessoa = pessoaRepository.findById(idpessoa).get();
        if (pessoa.getCurriculo() != null) {

            /* Setar tamanho da resposta */
            response.setContentLength(pessoa.getCurriculo().length);
            /* Seta tipo do arquivo para download */
            response.setContentType(pessoa.getTipoFileCurriculo());
            /* define o cabeçalho da resposta */
            String headerKey = "Content-Disposition";
            String headerValue = String.format("attachment; filename=\"%s\"", pessoa.getNomeFileCurriculo());
            response.setHeader(headerKey, headerValue);
            /* Finaliza a resposta passando o arquivo */
            response.getOutputStream().write(pessoa.getCurriculo());
        }
    }
}
