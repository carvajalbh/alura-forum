package br.com.alura.forum.controller;

import br.com.alura.forum.controller.dto.DetlahesDoTopicoDTO;
import br.com.alura.forum.controller.dto.TopicoDTO;
import br.com.alura.forum.controller.form.AtualizacaoTopicoForm;
import br.com.alura.forum.controller.form.TopicoForm;
import br.com.alura.forum.modelo.Topico;
import br.com.alura.forum.repository.CursoRepository;
import br.com.alura.forum.repository.TopicoRepository;
import java.net.URI;
import java.util.Optional;
import javax.transaction.Transactional;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/topicos")
public class TopicosController {

  @Autowired
  private TopicoRepository topicoRepository;

  @Autowired
  private CursoRepository cursoRepository;

  @GetMapping
  @Cacheable(value = "ListaDeTopicos")
  public Page<TopicoDTO> listar(@RequestParam(required = false) String nomeCurso,
      @PageableDefault(sort = "id", direction = Direction.ASC, page = 0, size = 10) Pageable paginacao) {
    Page<Topico> topicos;

    if (nomeCurso == null) {
      topicos = topicoRepository.findAll(paginacao);
    } else {
      topicos = topicoRepository.findByCursoNome(nomeCurso, paginacao);
    }
    return TopicoDTO.converter(topicos);
  }

  @PostMapping
  @Transactional
  @CacheEvict(value = "ListaDeTopicos", allEntries = true)
  public ResponseEntity<TopicoDTO> cadastrar(@RequestBody @Valid TopicoForm form, UriComponentsBuilder uriComponentsBuilder) {
    Topico topico = form.converter(cursoRepository);
    topicoRepository.save(topico);
    URI uri = uriComponentsBuilder.path("/topicos/{id}").buildAndExpand(topico.getId()).toUri();
    return ResponseEntity.created(uri).body(new TopicoDTO(topico));
  }

  @GetMapping("/{id}")
  public ResponseEntity<DetlahesDoTopicoDTO> detalhar(@PathVariable Long id) {
    Optional<Topico> optional = topicoRepository.findById(id);
    if (optional.isPresent()) {
      return ResponseEntity.ok(new DetlahesDoTopicoDTO(optional.get()));
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @PutMapping("/{id}")
  @Transactional
  @CacheEvict(value = "ListaDeTopicos", allEntries = true)
  public ResponseEntity<TopicoDTO> atualizar(@PathVariable Long id, @RequestBody @Valid AtualizacaoTopicoForm form) {
    Optional<Topico> optional = topicoRepository.findById(id);
    if (optional.isPresent()) {
      Topico topico = form.atualizar(id, topicoRepository);
      return ResponseEntity.ok(new TopicoDTO(topico));
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @DeleteMapping("/{id}")
  @Transactional
  @CacheEvict(value = "ListaDeTopicos", allEntries = true)
  public ResponseEntity<?> remover(@PathVariable Long id) {
    Optional<Topico> optional = topicoRepository.findById(id);
    if (optional.isPresent()) {
      topicoRepository.deleteById(id);
      return ResponseEntity.ok().build();
    } else {
      return ResponseEntity.notFound().build();
    }
  }

}