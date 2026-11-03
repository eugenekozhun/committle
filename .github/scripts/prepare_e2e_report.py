import argparse
import html
import shutil
import xml.etree.ElementTree as ElementTree
from pathlib import Path


def parse_arguments() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument("--input", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--result", required=True)
    parser.add_argument("--run-url", required=True)
    return parser.parse_args()


def escape(value: object) -> str:
    return html.escape(str(value), quote=True)


def main() -> None:
    args = parse_arguments()
    if args.output.exists():
        shutil.rmtree(args.output)
    args.output.mkdir(parents=True)

    html_report = args.input / "build/reports/tests/integrationTest"
    if html_report.exists():
        shutil.copytree(html_report, args.output / "tests")

    screenshots = sorted(args.input.glob("out/ide-tests/tests/*/*/log/screenshots/**/*.png"))
    published_screenshots = []
    for screenshot in screenshots:
        relative_path = screenshot.relative_to(args.input)
        scenario = relative_path.parts[4]
        destination_relative = Path("screenshots", scenario, *relative_path.parts[7:])
        destination = args.output / destination_relative
        destination.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(screenshot, destination)
        published_screenshots.append((scenario, screenshot.stem, destination_relative.as_posix()))

    suites = []
    totals = {"tests": 0, "failures": 0, "errors": 0, "skipped": 0}
    for result_file in sorted(args.input.glob("build/test-results/integrationTest/TEST-*.xml")):
        suite = ElementTree.parse(result_file).getroot()
        for key in totals:
            totals[key] += int(suite.attrib.get(key, 0))
        suites.append(suite.attrib)

    rows = "\n".join(
        "<tr>"
        f"<td>{escape(suite.get('name', '').rsplit('.', 1)[-1])}</td>"
        f"<td>{escape(suite.get('tests', 0))}</td>"
        f"<td>{escape(suite.get('failures', 0))}</td>"
        f"<td>{escape(suite.get('errors', 0))}</td>"
        f"<td>{escape(suite.get('skipped', 0))}</td>"
        f"<td>{escape(suite.get('time', '0'))} s</td>"
        "</tr>"
        for suite in suites
    ) or "<tr><td colspan=\"6\">No JUnit results were generated.</td></tr>"

    screenshot_sections = []
    for scenario, name, source in published_screenshots:
        screenshot_sections.append(
            f"<figure><figcaption>{escape(scenario)} — {escape(name)}</figcaption>"
            f"<a href=\"{escape(source)}\"><img src=\"{escape(source)}\" loading=\"lazy\" "
            f"alt=\"{escape(scenario)} screenshot\"></a></figure>"
        )
    screenshots_html = "\n".join(screenshot_sections) or "<p>No screenshots were generated.</p>"
    report_link = (
        '<a class="button" href="tests/index.html">Open detailed Gradle HTML report</a>'
        if html_report.exists()
        else "<p>The Gradle HTML report was not generated.</p>"
    )

    document = f"""<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Committle E2E report</title>
  <style>
    body {{ font-family: system-ui, sans-serif; max-width: 1200px; margin: 2rem auto; padding: 0 1rem; color: #1f2328; }}
    .status {{ display: inline-block; padding: .35rem .7rem; border-radius: .5rem; font-weight: 600; }}
    .success {{ background: #dafbe1; color: #116329; }}
    .failure {{ background: #ffebe9; color: #a40e26; }}
    .metrics {{ display: flex; gap: 1rem; flex-wrap: wrap; margin: 1.5rem 0; }}
    .metric {{ background: #f6f8fa; border-radius: .5rem; padding: 1rem; min-width: 7rem; }}
    .metric strong {{ display: block; font-size: 1.6rem; }}
    table {{ border-collapse: collapse; width: 100%; margin-bottom: 2rem; }}
    th, td {{ border-bottom: 1px solid #d1d9e0; padding: .65rem; text-align: left; }}
    .button {{ display: inline-block; padding: .7rem 1rem; background: #0969da; color: white; border-radius: .4rem; text-decoration: none; }}
    figure {{ margin: 2rem 0; }}
    figcaption {{ font-weight: 600; margin-bottom: .6rem; }}
    img {{ display: block; max-width: 100%; border: 1px solid #d1d9e0; border-radius: .4rem; }}
  </style>
</head>
<body>
  <h1>Committle E2E report</h1>
  <p>Result: <span class="status {escape(args.result)}">{escape(args.result)}</span></p>
  <p><a href="{escape(args.run_url)}">Open the GitHub Actions run</a></p>
  <div class="metrics">
    <div class="metric"><strong>{totals['tests']}</strong>Tests</div>
    <div class="metric"><strong>{totals['failures'] + totals['errors']}</strong>Failures</div>
    <div class="metric"><strong>{totals['skipped']}</strong>Skipped</div>
  </div>
  {report_link}
  <h2>Test suites</h2>
  <table>
    <thead><tr><th>Suite</th><th>Tests</th><th>Failures</th><th>Errors</th><th>Skipped</th><th>Time</th></tr></thead>
    <tbody>{rows}</tbody>
  </table>
  <h2>Screenshots</h2>
  {screenshots_html}
</body>
</html>
"""
    (args.output / "index.html").write_text(document, encoding="utf-8")
    (args.output / ".nojekyll").write_text("", encoding="utf-8")


if __name__ == "__main__":
    main()
