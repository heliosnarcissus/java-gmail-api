<script>
  import { url, layout } from "@sveltech/routify";

  const posts = $layout.parent.children
   .filter((c) => c.meta["frontmatter"]);
    //.sort((a, b) => b.meta["frontmatter"].published.localeCompare(a.meta["frontmatter"].published));


    console.log(posts);
</script>

<h1 class="bg-teal-500">Helios</h1>

<ul class="posts">
  {#each posts as {meta, path}}
    <li class="card">
      <a class="title" href={$url(path)}>{meta.frontmatter.title}</a>
      {@html (meta.frontmatter.summary)}
    </li>
  {/each}
</ul>
