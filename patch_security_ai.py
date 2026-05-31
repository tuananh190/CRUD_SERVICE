import sys

def patch_topic_service():
    filepath = "src/main/java/com/mar/CRUD_SERVICE/service/TopicAnalysisService.java"
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    target_call = "ResponseEntity<Map> response = restTemplate.postForEntity(geminiApiUrl, request, Map.class);"
    repl_call = "ResponseEntity<String> response = restTemplate.postForEntity(geminiApiUrl, request, String.class);"

    target_parse = """            Object choicesObj = response.getBody().get("choices");
            if (!(choicesObj instanceof List<?> choices) || choices.isEmpty()) {
                return Collections.emptyList();
            }
            Object first = choices.get(0);
            if (!(first instanceof Map<?, ?> firstMap)) {
                return Collections.emptyList();
            }
            Object messageObj = firstMap.get("message");
            if (!(messageObj instanceof Map<?, ?> msgMap)) {
                return Collections.emptyList();
            }
            Object contentObj = msgMap.get("content");
            if (!(contentObj instanceof String text)) {
                return Collections.emptyList();
            }

            text = text.trim();
            if (text.startsWith("```json")) {
                text = text.substring(7);
            }
            if (text.startsWith("```")) {
                text = text.substring(3);
            }
            if (text.endsWith("```")) {
                text = text.substring(0, text.length() - 3);
            }
            text = text.trim();

            List<String> topics = new ArrayList<>();
            if (text.startsWith("[") && text.endsWith("]")) {
                text = text.substring(1, text.length() - 1);
            }
            for (String raw : text.split("[,\n]")) {
                String t = raw.replace("\"", "").replace("'", "").trim();
                if (!t.isEmpty()) {
                    topics.add(t);
                }
            }
            return topics;"""

    repl_parse = """            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode rootNode = mapper.readTree(response.getBody());
            com.fasterxml.jackson.databind.JsonNode choices = rootNode.path("choices");
            if (choices.isMissingNode() || !choices.isArray() || choices.isEmpty()) {
                return Collections.emptyList();
            }
            
            String text = choices.get(0).path("message").path("content").asText();
            text = text.trim();
            if (text.startsWith("```json")) {
                text = text.substring(7);
            } else if (text.startsWith("```")) {
                text = text.substring(3);
            }
            if (text.endsWith("```")) {
                text = text.substring(0, text.length() - 3);
            }
            text = text.trim();

            List<String> topics = new ArrayList<>();
            com.fasterxml.jackson.databind.JsonNode topicsNode = mapper.readTree(text);
            if (topicsNode.isArray()) {
                for (com.fasterxml.jackson.databind.JsonNode node : topicsNode) {
                    if (node.isTextual() && !node.asText().trim().isEmpty()) {
                        topics.add(node.asText().trim());
                    }
                }
            }
            return topics;"""

    content = content.replace(target_call, repl_call)
    content = content.replace(target_parse, repl_parse)
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

def patch_user_service():
    filepath = "src/main/java/com/mar/CRUD_SERVICE/controller/UserController.java"
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    update_target = """    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody UserCreationRequest request){
        try{
            User updated = userService.updateUser(id, request);
            return ResponseEntity.ok(updated);
        }catch(IllegalStateException e){
            return ResponseEntity.notFound().build();
        }
    }"""
    
    update_repl = """    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody UserCreationRequest request, Principal principal){
        try{
            User targetUser = userService.getUserById(id)
                    .orElseThrow(() -> new IllegalStateException("User not found"));
            
            boolean isAdmin = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication().getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                    
            if (principal == null || (!targetUser.getUsername().equals(principal.getName()) && !isAdmin)) {
                throw new com.mar.CRUD_SERVICE.exception.AccessDeniedException("Bạn không có quyền sửa thông tin người khác");
            }
            User updated = userService.updateUser(id, request);
            return ResponseEntity.ok(updated);
        }catch(IllegalStateException e){
            return ResponseEntity.notFound().build();
        }
    }"""
    
    delete_target = """    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id){
        try{
            userService.deleteUser(id);
            return ResponseEntity.ok(new ApiResponse<>(200, "Xóa người dùng thành công", null));
        }catch(IllegalStateException e){
            return ResponseEntity.notFound().build();
        }
    }"""
    
    delete_repl = """    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, Principal principal){
        try{
            User targetUser = userService.getUserById(id)
                    .orElseThrow(() -> new IllegalStateException("User not found"));
            
            boolean isAdmin = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication().getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                    
            if (principal == null || (!targetUser.getUsername().equals(principal.getName()) && !isAdmin)) {
                throw new com.mar.CRUD_SERVICE.exception.AccessDeniedException("Bạn không có quyền xóa tài khoản của người khác");
            }
            userService.deleteUser(id);
            return ResponseEntity.ok(new ApiResponse<>(200, "Xóa người dùng thành công", null));
        }catch(IllegalStateException e){
            return ResponseEntity.notFound().build();
        }
    }"""
    
    content = content.replace(update_target, update_repl)
    content = content.replace(delete_target, delete_repl)
    
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

if __name__ == '__main__':
    patch_topic_service()
    patch_user_service()
    print("Patch applied successfully.")
