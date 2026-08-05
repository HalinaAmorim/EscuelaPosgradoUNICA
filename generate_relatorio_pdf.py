# -*- coding: utf-8 -*-
"""Gera relatório PDF de análise de manutenibilidade (SonarQube + Lizard)."""
from pathlib import Path
from datetime import datetime
from fpdf import FPDF

OUT = Path(r"C:\Users\Halina\EscuelaPosgradoUNICA\Relatorio_Manutenibilidade_Backend.pdf")
FONT_DIR = Path(r"C:\Windows\Fonts")
FONT_REG = FONT_DIR / "arial.ttf"
FONT_BOLD = FONT_DIR / "arialbd.ttf"
FONT_ITALIC = FONT_DIR / "ariali.ttf"


class ReportPDF(FPDF):
    def __init__(self):
        super().__init__(orientation="P", unit="mm", format="A4")
        self.set_auto_page_break(auto=True, margin=18)
        self.add_font("ArialUni", "", str(FONT_REG))
        self.add_font("ArialUni", "B", str(FONT_BOLD))
        self.add_font("ArialUni", "I", str(FONT_ITALIC))
        self.alias_nb_pages()

    def header(self):
        if self.page_no() == 1:
            return
        self.set_font("ArialUni", "I", 8)
        self.set_text_color(100, 100, 100)
        self.cell(0, 6, "Escuela Posgrado UNICA — Relatório de Manutenibilidade Backend", align="L")
        self.ln(2)
        self.set_draw_color(0, 82, 147)
        self.set_line_width(0.3)
        self.line(10, 12, 200, 12)
        self.ln(6)

    def footer(self):
        self.set_y(-12)
        self.set_font("ArialUni", "I", 8)
        self.set_text_color(120, 120, 120)
        self.cell(0, 8, f"Página {self.page_no()}/{{nb}}", align="C")

    def h1(self, text):
        self.set_font("ArialUni", "B", 16)
        self.set_text_color(0, 82, 147)
        self.multi_cell(0, 9, text)
        self.ln(2)

    def h2(self, text):
        self.ln(2)
        self.set_font("ArialUni", "B", 12)
        self.set_text_color(0, 82, 147)
        self.multi_cell(0, 7, text)
        self.ln(1)

    def h3(self, text):
        self.ln(1)
        self.set_font("ArialUni", "B", 10)
        self.set_text_color(30, 30, 30)
        self.multi_cell(0, 6, text)
        self.ln(0.5)

    def body(self, text):
        self.set_x(self.l_margin)
        self.set_font("ArialUni", "", 9)
        self.set_text_color(30, 30, 30)
        self.multi_cell(0, 5, text)
        self.set_x(self.l_margin)
        self.ln(1)

    def bullet(self, text):
        self.set_x(self.l_margin)
        self.set_font("ArialUni", "", 9)
        self.set_text_color(30, 30, 30)
        self.multi_cell(0, 5, f"-  {text}")
        self.set_x(self.l_margin)

    def note(self, text):
        self.set_font("ArialUni", "I", 8)
        self.set_text_color(80, 80, 80)
        self.multi_cell(0, 4.5, text)
        self.ln(1)

    def kv_table(self, rows, col_widths=(70, 120)):
        self.set_font("ArialUni", "", 9)
        for i, (k, v) in enumerate(rows):
            bg = (240, 246, 252) if i % 2 == 0 else (255, 255, 255)
            self.set_fill_color(*bg)
            self.set_text_color(0, 82, 147)
            self.set_font("ArialUni", "B", 9)
            self.cell(col_widths[0], 6, k, border=0, fill=True)
            self.set_text_color(30, 30, 30)
            self.set_font("ArialUni", "", 9)
            self.cell(col_widths[1], 6, str(v), border=0, fill=True)
            self.ln()
        self.ln(2)

    def simple_table(self, headers, rows, col_widths):
        usable = 190
        if sum(col_widths) > usable:
            scale = usable / sum(col_widths)
            col_widths = [w * scale for w in col_widths]

        self.set_font("ArialUni", "B", 7)
        self.set_fill_color(0, 82, 147)
        self.set_text_color(255, 255, 255)
        for h, w in zip(headers, col_widths):
            self.cell(w, 6, h, border=0, fill=True, align="C")
        self.ln()

        self.set_text_color(30, 30, 30)
        for i, row in enumerate(rows):
            # page break if needed
            if self.get_y() > 270:
                self.add_page()
                self.set_font("ArialUni", "B", 7)
                self.set_fill_color(0, 82, 147)
                self.set_text_color(255, 255, 255)
                for h, w in zip(headers, col_widths):
                    self.cell(w, 6, h, border=0, fill=True, align="C")
                self.ln()
                self.set_text_color(30, 30, 30)

            bg = (245, 248, 252) if i % 2 == 0 else (255, 255, 255)
            self.set_fill_color(*bg)
            self.set_font("ArialUni", "", 6.5)
            # compute row height from tallest cell
            line_h = 4
            max_lines = 1
            for cell, w in zip(row, col_widths):
                lines = self.multi_cell(w, line_h, str(cell), dry_run=True, output="LINES")
                max_lines = max(max_lines, len(lines))
            row_h = max(line_h * max_lines + 1, 6)
            x0, y0 = self.get_x(), self.get_y()
            for j, (cell, w) in enumerate(zip(row, col_widths)):
                self.set_xy(x0 + sum(col_widths[:j]), y0)
                self.rect(x0 + sum(col_widths[:j]), y0, w, row_h, style="F")
                self.set_xy(x0 + sum(col_widths[:j]), y0 + 0.5)
                self.multi_cell(w, line_h, str(cell), align="L")
            self.set_xy(self.l_margin, y0 + row_h)
        self.set_x(self.l_margin)
        self.ln(3)


def build():
    pdf = ReportPDF()
    pdf.add_page()

    # Cover
    pdf.ln(30)
    pdf.set_font("ArialUni", "B", 22)
    pdf.set_text_color(0, 82, 147)
    pdf.multi_cell(0, 10, "Relatório de Análise de Manutenibilidade", align="C")
    pdf.ln(4)
    pdf.set_font("ArialUni", "B", 14)
    pdf.set_text_color(50, 50, 50)
    pdf.multi_cell(0, 8, "Backend — Escuela de Posgrado UNICA", align="C")
    pdf.ln(8)
    pdf.set_draw_color(0, 82, 147)
    pdf.set_line_width(0.8)
    pdf.line(60, pdf.get_y(), 150, pdf.get_y())
    pdf.ln(10)
    pdf.set_font("ArialUni", "", 11)
    pdf.set_text_color(60, 60, 60)
    pdf.multi_cell(
        0,
        6,
        "Análise baseada em métricas reais de SonarQube e Lizard\n"
        "Arquitetura de Software · Qualidade de Código · SOLID · Clean Code",
        align="C",
    )
    pdf.ln(15)
    pdf.set_font("ArialUni", "", 10)
    pdf.multi_cell(
        0,
        6,
        f"Data: {datetime.now().strftime('%d/%m/%Y %H:%M')}\n"
        "Microsserviços: Autenticacion · Intranet · Matricula\n"
        "SonarQube 26.7 · Lizard 1.23.0\n"
        "Projeto Sonar: escuela-posgrado-unica-backend",
        align="C",
    )
    pdf.ln(20)
    pdf.set_font("ArialUni", "I", 9)
    pdf.set_text_color(100, 100, 100)
    pdf.multi_cell(
        0,
        5,
        "Documento gerado para priorização de refatoração.\n"
        "Nenhuma alteração de código foi aplicada neste ciclo de análise.",
        align="C",
    )

    # 1. Resumo executivo
    pdf.add_page()
    pdf.h1("1. Resumo Executivo")
    pdf.body(
        "O backend Java (Spring Boot) dos microsserviços Autenticacion, Intranet e Matricula "
        "foi analisado com SonarQube e Lizard. Não foram detectados bugs nem vulnerabilidades "
        "pelo SonarQube. A dívida técnica total é de aproximadamente 51,7 horas (3.101 minutos), "
        "com 356 code smells e 10,1% de duplicação. A cobertura de testes é 0%."
    )
    pdf.body(
        "As classes mais críticas concentram-se em Autenticacion e Intranet. Matricula apresenta "
        "perfil relativamente mais saudável. Destacam-se: UsuarioService (maior debt), "
        "EncuestaService, GoogleOAuthService, AdminController, AuthService e o par "
        "ExcelService/ExcelServiceNew (duplicação 89,8%)."
    )

    pdf.h2("1.1 Métricas do Projeto (SonarQube)")
    pdf.kv_table(
        [
            ("LOC (ncloc)", "10.070"),
            ("Bugs", "0"),
            ("Vulnerabilidades", "0"),
            ("Code Smells", "356 (49 Critical · 117 Major · 136 Minor · 54 Info)"),
            ("Security Hotspots", "0"),
            ("Duplicação", "10,1% (39 blocos / 27 arquivos)"),
            ("Technical Debt", "3.101 min (~51,7 h) · debt ratio 1,0%"),
            ("Maintainability Rating", "A"),
            ("Reliability Rating", "A"),
            ("Security Rating", "A"),
            ("Cobertura de Testes", "0,0%"),
            ("Complexidade / Cognitiva", "1.441 / 547"),
        ]
    )

    pdf.h2("1.2 Métricas Globais (Lizard)")
    pdf.kv_table(
        [
            ("NLOC total (backend)", "14.232"),
            ("Funções analisadas", "2.078"),
            ("Avg CCN", "1,3"),
            ("Avg NLOC / função", "4,1"),
        ]
    )
    pdf.note(
        "Nota: Coesão, Acoplamento e Índice de Manutenibilidade (MI) por classe são estimativas "
        "técnicas (inspeção de código + métricas Sonar/Lizard), pois não há LCOM/CBO/MI automáticos no pipeline."
    )

    # 2. Critérios
    pdf.h2("2. Critérios de Ordenação das Classes Críticas")
    pdf.body("As classes foram ordenadas nesta prioridade:")
    for i, c in enumerate(
        [
            "Technical Debt (SonarQube sqale_index)",
            "Bugs",
            "Vulnerabilidades",
            "Code Smells",
            "Complexidade Ciclomática (Lizard / Sonar)",
            "Acoplamento (estimativa)",
            "Baixa Coesão (estimativa)",
            "Baixo Índice de Manutenibilidade (estimativa)",
            "Duplicação",
            "LOC",
        ],
        1,
    ):
        pdf.bullet(f"{i}. {c}")

    # 3. Top 10 table
    pdf.add_page()
    pdf.h1("3. Top 10 Classes Mais Críticas")
    pdf.body(
        "Escopo: Controllers, Services e componentes de negócio. "
        "DTOs simples, entidades anêmicas, configs e testes foram excluídos "
        "(salvo quando relevantes, ex.: duplicação Excel)."
    )

    headers = [
        "Classe",
        "Smells",
        "Cplx",
        "LOC",
        "Dup%",
        "Debt(min)",
        "MI*",
        "Coesão*",
        "Acopl*",
        "Prio",
    ]
    widths = [38, 12, 12, 12, 12, 16, 12, 16, 14, 12]
    rows = [
        ["UsuarioService", "18", "64/6", "277", "0", "227", "~44", "Baixa", "Méd(2)", "P1"],
        ["EncuestaService", "19", "57/9", "248", "0", "186", "~52", "Média", "Alto(5)", "P1"],
        ["GoogleOAuthService", "11", "53/9", "229", "5,6", "150", "~45", "Média", "Méd(3)", "P1"],
        ["AdminController", "15", "23/5", "601", "0", "145", "~48", "Média", "Méd(3)", "P2"],
        ["AuthService", "23", "87/15", "361", "15,4", "130", "~42", "Baixa", "Méd(4)", "P1"],
        ["ExcelService", "9", "47/12", "289", "89,8", "130", "~40", "Alta", "Baixo(1)", "P0"],
        ["ExcelServiceNew", "9", "47/12", "289", "89,8", "130", "~40", "— morta", "—", "P0"],
        ["CalificacionService", "13", "63/5", "256", "0", "114", "~50", "Média", "Méd(3)", "P2"],
        ["AsistenciaService", "11", "43/~4", "192", "0", "104", "~55", "Média", "Méd", "P2"],
        ["TurnoMatriculaService", "15", "41/9", "209", "0", "87", "~62", "Alta", "Méd(3)", "P3"],
    ]
    pdf.simple_table(headers, rows, widths)
    pdf.note(
        "Bugs = 0 e Vulnerabilidades = 0 em todas. Cplx = Complexity Sonar / Max CCN Lizard. "
        "* = estimativa técnica. AdminController: ~13 smells são caractere U+200D em literais Swagger (debt parcialmente cosmético)."
    )

    # 4. Detalhamento
    pdf.add_page()
    pdf.h1("4. Detalhamento das Classes Críticas")

    details = [
        (
            "4.1 UsuarioService (Intranet) — Debt 227 min",
            [
                "Responsabilidade: Gestão de usuários na Intranet com duas APIs paralelas (entidade + DTO) e soft-delete inconsistente.",
                "Por que crítica: Maior technical debt do backend; 18 smells; baixa coesão.",
                "Métodos críticos: crearUsuario(UsuarioDTO), actualizarUsuario; duplicatas entity/DTO.",
                "SOLID: SRP (dois estilos de API), OCP (helpers por role fixos).",
                "Code Smells Sonar: RuntimeException genérica (S112×8), Collectors.toList (S6204), literais duplicados (S1192), timezone (S8688).",
                "Métricas: Debt 227 · Smells 18 · Complexity 64 · MI~44 (est.).",
            ],
        ),
        (
            "4.2 EncuestaService (Intranet) — Debt 186 min",
            [
                "Responsabilidade: CRUD de encuestas, respostas e agregação de resultados (DTOs embutidos no arquivo).",
                "Por que crítica: 2º maior debt; acoplamento a 5 repositórios; responderEncuesta com CCN 9.",
                "Métodos críticos: responderEncuesta, obtenerResultadosEncuesta.",
                "SOLID: SRP (comando + query + DTOs), DIP fraco (repos concretos).",
                "Code Smells: S112×5, S6204×5, Boolean boxing (S5411), literais repetidos.",
                "Métricas: Debt 186 · Smells 19 · Max CCN 9 · MI~52 (est.).",
            ],
        ),
        (
            "4.3 GoogleOAuthService (Autenticacion) — Debt 150 min",
            [
                "Responsabilidade: OAuth Google, provisionamento de usuário, emissão JWT e heurística de roles.",
                "Por que crítica: Fluxo sensível de segurança; nested try; role por substring do e-mail; Cog 37.",
                "Métodos críticos: verifyGoogleToken (CCN 9), determineRoleFromEmail, authenticateWithGoogle.",
                "SOLID: SRP e OCP violados (heurísticas hard-coded).",
                "Code Smells: S112×4, nested try (S1141), timezone (S8688).",
                "Métricas: Debt 150 · Cog 37 · Dup 5,6% · MI~45 (est.).",
            ],
        ),
        (
            "4.4 AdminController (Autenticacion) — Debt 145 min",
            [
                "Responsabilidade: API admin (users + cleanup + Excel).",
                "Por que crítica: 601 LOC; múltiplos concerns; debt inflado por unicode em Swagger.",
                "Métodos: importarUsuariosExcel, limpiarDuplicados, endpoints por role.",
                "SOLID: SRP (users + Excel + cleanup no mesmo controller).",
                "Code Smells: S2479×13 (\\u200D), package naming, timezone.",
                "Métricas: Debt 145 · LOC 601 · Smells 15 · MI~48 (est.).",
            ],
        ),
        (
            "4.5 AuthService (Autenticacion) — Debt 130 min",
            [
                "Responsabilidade: Login/JWT e administração de usuários no mesmo service.",
                "Por que crítica: Max CCN 15 (setRoleSpecificFields); Cog 79; Dup 15,4%; 23 smells.",
                "Métodos: setRoleSpecificFields (CCN 15), actualizarUsuarioAdmin (CCN 13), validateUniqueFields (CCN 12).",
                "SOLID: SRP, OCP (switch em Role), DIP.",
                "Code Smells: Cognitive Complexity >15 (S3776), ifs aninhados (S1066), switch cases (S6208).",
                "Métricas: Complexity 87 · Max CCN 15 · Dup 15,4% · MI~42 (est.).",
            ],
        ),
        (
            "4.6 / 4.7 ExcelService e ExcelServiceNew — Debt 130 min cada",
            [
                "Responsabilidade: Import/export Excel de usuários via Apache POI.",
                "Por que crítica: Classes quase idênticas (Dup 89,8%); ExcelServiceNew não referenciada (código morto).",
                "Métodos: importUsuariosFromExcel (CCN 12, Cog 17), mapRowToRegistroRequest (CCN 10).",
                "SOLID: OCP (colunas mágicas); Feature Envy em direção a AuthService.",
                "Ação de alto ROI: remover ExcelServiceNew e refatorar ExcelService.",
                "Métricas: Debt 130 cada · Dup 89,8% · Max CCN 12 · MI~40 (est.).",
            ],
        ),
        (
            "4.8 CalificacionService — Debt 114 min",
            [
                "CRUD de notas + ranking + média ponderada; histórico embutido em observaciones.",
                "Métodos: obtenerRankingEstudiantes, calcularPromedioPonderado.",
                "Métricas: Debt 114 · Smells 13 · Complexity 63 · MI~50 (est.).",
            ],
        ),
        (
            "4.9 AsistenciaService — Debt 104 min",
            [
                "Padrões semelhantes ao restante do Intranet (exceções genéricas, Collectors.toList).",
                "Métricas: Debt 104 · Smells 11 · Complexity 43 · MI~55 (est.).",
            ],
        ),
        (
            "4.10 TurnoMatriculaService — Debt 87 min",
            [
                "create/update com validações duplicadas; Max CCN 9; coesão alta (Matrícula mais saudável).",
                "Métricas: Debt 87 · Smells 15 · Max CCN 9 · MI~62 (est.).",
            ],
        ),
    ]

    for title, bullets in details:
        pdf.h3(title)
        for b in bullets:
            pdf.bullet(b)
        pdf.ln(1)

    pdf.h3("Achado transversal: JwtUtils triplicado")
    pdf.bullet(
        "JwtUtils existe em Autenticacion, Intranet e Matricula. Secrets default divergentes entre módulos "
        "(risco operacional). Debt Sonar por arquivo ~25 min, mas impacto sistêmico alto."
    )

    # 5. Plano top 5
    pdf.add_page()
    pdf.h1("5. Plano de Refatoração — Top 5 Classes")
    pdf.body(
        "Seleção conforme ordenação por Technical Debt. Nenhuma alteração foi aplicada; "
        "métricas 'depois' são projeções esperadas."
    )

    plan_headers = ["Nº", "Classe", "Método/Trecho", "Problema", "Antes", "Refatoração", "Depois (esp.)"]
    plan_widths = [8, 28, 32, 35, 28, 35, 28]
    plan_rows = [
        [
            "1",
            "UsuarioService",
            "crearUsuario / actualizarUsuario",
            "Dual API entity+DTO; SRP; S112",
            "Debt 227 · Smells 18 · Cplx 64",
            "Unificar DTO + CQRS + exceptions tipadas",
            "Debt ~80 · Smells ~6 · Cplx ~35",
        ],
        [
            "2",
            "EncuestaService",
            "responderEncuesta",
            "God service; CCN 9; 5 repos",
            "Debt 186 · MaxCCN 9",
            "Separar write/read + extrair DTOs",
            "Debt ~70 · MaxCCN ≤5",
        ],
        [
            "3",
            "GoogleOAuthService",
            "verifyGoogleToken",
            "Nested try; role heurística",
            "Debt 150 · Cog 37",
            "Verifier + RoleResolver + aud check",
            "Debt ~40 · Cog ≤15",
        ],
        [
            "4",
            "AdminController",
            "Swagger / Excel endpoints",
            "U+200D ×13; controller gordo",
            "Debt 145 · LOC 601",
            "Limpar unicode; split controllers",
            "Debt ~20 · LOC ~250",
        ],
        [
            "5",
            "AuthService",
            "setRoleSpecificFields",
            "Switch Role; CCN 15; Dup 15,4%",
            "Debt 130 · MaxCCN 15 · Dup 15,4%",
            "Split Auth/Admin + Strategy",
            "Debt ~40 · MaxCCN ≤8 · Dup <3%",
        ],
    ]
    pdf.simple_table(plan_headers, plan_rows, plan_widths)

    pdf.h2("5.1 Detalhe da Refatoração Sugerida")

    pdf.h3("1) UsuarioService")
    pdf.bullet("Unificar em API DTO-only; UsuarioCommandService + UsuarioQueryService.")
    pdf.bullet("Exceptions tipadas; alinhar soft-delete (activo vs eliminado).")
    pdf.bullet("Princípios: SRP, CQRS leve, DIP.")

    pdf.h3("2) EncuestaService")
    pdf.bullet("Extrair DTOs para pacote próprio.")
    pdf.bullet("EncuestaResponseService (write) + EncuestaResultadosService (read).")
    pdf.bullet("Princípios: SRP, CQRS, Repository.")

    pdf.h3("3) GoogleOAuthService")
    pdf.bullet("Extrair GoogleTokenVerifier; RoleResolver configurável.")
    pdf.bullet("Validar aud/azp do token; reutilizar mapper de AuthService.")
    pdf.bullet("Princípios: SRP, Strategy, DIP.")

    pdf.h3("4) AdminController")
    pdf.bullet("Remover caracteres U+200D dos literais Swagger.")
    pdf.bullet("Separar AdminUserController + AdminExcelController; CORS externalizado.")
    pdf.bullet("Princípios: SRP.")

    pdf.h3("5) AuthService")
    pdf.bullet("AuthenticationService + UserAdminService.")
    pdf.bullet("Strategy/Map por Role; eliminar overload duplicado de setRoleSpecificFields.")
    pdf.bullet("Princípios: SRP, OCP, Strategy.")

    pdf.h3("Bônus P0 — ExcelService / ExcelServiceNew")
    pdf.bullet(
        "Deletar ExcelServiceNew (código morto) e manter/refatorar ExcelService. "
        "Elimina ~289 LOC e ~89,8% de duplicação do par — melhor ROI imediato."
    )

    # 6. Ranking
    pdf.add_page()
    pdf.h1("6. Ranking Final e Justificativa")

    ranking = [
        ("1º UsuarioService", "Maior technical debt (227 min) e menor coesão (duas APIs no mesmo service)."),
        ("2º EncuestaService", "2º debt (186); acoplamento alto (5 repos); orquestra CRUD + analytics."),
        ("3º GoogleOAuthService", "Debt 150; segurança com Cog 37 e regras frágeis de role."),
        ("4º AdminController", "Debt 145 e 601 LOC; superfície admin misturada; parte do debt é cosmético (ZWJ)."),
        ("5º AuthService", "23 smells, CCN 15, Cog 79 e Dup 15,4% — núcleo do domínio de autenticação."),
        ("6º ExcelService / ExcelServiceNew", "Duplicação 89,8%; New é código morto — alto ROI."),
        ("7º CalificacionService", "Debt 114; ranking/médias e smells de estilo Intranet."),
        ("8º AsistenciaService", "Debt 104; padrões repetidos do módulo Intranet."),
        ("9º TurnoMatriculaService", "Debt 87; validações create/update duplicadas; coesão alta."),
        ("10º Próximos", "AulaService / ProgramaEstudioService (debt ~72–80 min)."),
    ]
    for title, reason in ranking:
        pdf.h3(title)
        pdf.body(reason)

    pdf.h2("Por que priorizar assim?")
    pdf.body(
        "Com Bugs = 0 e Vulnerabilidades = 0, o critério dominante é Technical Debt do SonarQube. "
        "Nos empates, entram quantidade de smells, CCN do Lizard e duplicação. "
        "Autenticacion e Intranet concentram a dívida; Matricula está relativamente mais saudável."
    )

    # 7. Ordem sugerida
    pdf.h2("7. Ordem Sugerida de Execução (após aprovação)")
    for i, item in enumerate(
        [
            "ExcelServiceNew (delete — rápido, alto ROI)",
            "AuthService (impacto estrutural no domínio auth)",
            "UsuarioService",
            "GoogleOAuthService",
            "EncuestaService",
            "AdminController (limpeza + split)",
        ],
        1,
    ):
        pdf.bullet(f"{i}. {item}")

    pdf.ln(4)
    pdf.h2("8. Fontes e Artefatos")
    pdf.bullet("SonarQube Dashboard: http://localhost:9000/dashboard?id=escuela-posgrado-unica-backend")
    pdf.bullet("Lizard: lizard-full.txt / lizard-report.csv")
    pdf.bullet("Sonar exports: sonar-project-measures.txt, sonar-file-measures.json, sonar-issues.json")
    pdf.bullet("Configuração: sonar-project.properties")

    pdf.ln(6)
    pdf.set_font("ArialUni", "I", 9)
    pdf.set_text_color(80, 80, 80)
    pdf.multi_cell(
        0,
        5,
        "Fim do relatório. Aguardando aprovação das classes selecionadas antes de qualquer alteração de código.",
    )

    pdf.output(str(OUT))
    print(f"PDF_OK={OUT}")
    print(f"SIZE_BYTES={OUT.stat().st_size}")


if __name__ == "__main__":
    build()
