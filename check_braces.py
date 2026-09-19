import sys
src = open(r'app/src/main/java/com/yxlanyu/refreshrate/service/KeepAliveAccessibilityService.java', encoding='utf-8').read()
stack = []
in_str = in_char = in_line = in_block = False
pairs = {'(': ')', '{': '}', '[': ']'}
i = 0
n = len(src)
while i < n:
    ch = src[i]
    if in_line:
        if ch == '\n':
            in_line = False
        i += 1
        continue
    if in_block:
        if ch == '*' and i + 1 < n and src[i + 1] == '/':
            in_block = False
            i += 2
            continue
        i += 1
        continue
    if in_str:
        if ch == '\\':
            i += 2
            continue
        if ch == '"':
            in_str = False
        i += 1
        continue
    if in_char:
        if ch == '\\':
            i += 2
            continue
        if ch == "'":
            in_char = False
        i += 1
        continue
    if ch == '/':
        if i + 1 < n and src[i + 1] == '/':
            in_line = True
            i += 2
            continue
        if i + 1 < n and src[i + 1] == '*':
            in_block = True
            i += 2
            continue
        i += 1
        continue
    if ch == '"':
        in_str = True
        i += 1
        continue
    if ch == "'":
        in_char = True
        i += 1
        continue
    if ch in pairs:
        stack.append((ch, i))
    elif ch in ')]}':
        if not stack:
            print('UNMATCHED close', ch, i)
            sys.exit(1)
        o, i0 = stack.pop()
        if pairs[o] != ch:
            print('MISMATCH', o, i0, ch, i)
            sys.exit(1)
    i += 1
print('unclosed:', [(c, i) for c, i in stack])